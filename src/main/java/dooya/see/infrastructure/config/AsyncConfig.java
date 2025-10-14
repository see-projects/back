package dooya.see.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 12;
    private static final int QUEUE_CAPACITY = 200;
    private static final long RETRY_BACKOFF_MILLIS = 100L;
    private static final int MAX_ATTEMPTS = 3;

    @Bean(name = "applicationTaskExecutor")
    @ConditionalOnMissingBean(name = "applicationTaskExecutor")
    public Executor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("see-async-");
        executor.setTaskDecorator(new LoggingRetryingTaskDecorator());
        executor.setRejectedExecutionHandler(this::handleRejectedExecution);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return applicationTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncExceptionHandler();
    }

    private void handleRejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        RejectedExecutionException exception = new RejectedExecutionException("Async task queue is full");
        log.error("Rejected async task submission. ActiveCount={}, QueueSize={}",
                executor.getActiveCount(), executor.getQueue().size(), exception);
        throw exception;
    }

    private static class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("Uncaught async exception in {} with params {}", method, params, ex);
        }
    }

    private static class LoggingRetryingTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                int attempt = 1;
                while (true) {
                    try {
                        runnable.run();
                        return;
                    } catch (Throwable ex) {
                        log.error("Async task failed on attempt {}", attempt, ex);
                        if (attempt++ >= MAX_ATTEMPTS) {
                            if (ex instanceof RuntimeException runtimeException) {
                                throw runtimeException;
                            }
                            throw new RuntimeException(ex);
                        }
                        try {
                            Thread.sleep(RETRY_BACKOFF_MILLIS);
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            if (ex instanceof RuntimeException runtimeException) {
                                throw runtimeException;
                            }
                            throw new RuntimeException(ex);
                        }
                    }
                }
            };
        }
    }
}
