package com.benmohammad.javamvi.stats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.javamvi.mvibase.MviResult;
import com.benmohammad.javamvi.util.LceStatus;
import com.google.auto.value.AutoValue;

import static com.benmohammad.javamvi.util.LceStatus.FAILURE;
import static com.benmohammad.javamvi.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.javamvi.util.LceStatus.SUCCESS;

public interface StatisticsResult extends MviResult {

    @AutoValue
    abstract class LoadStatistics implements StatisticsResult {
        @NonNull
        abstract LceStatus status();

        abstract int activeCount();
        abstract int completedCount();

        @Nullable
        abstract Throwable error();

        @NonNull
        static LoadStatistics success(int activeCount, int completedCount) {
            return new AutoValue_StatisticsResult_LoadStatistics(SUCCESS, activeCount, completedCount, null);
        }

        @NonNull
        static LoadStatistics failure(Throwable error) {
            return new AutoValue_StatisticsResult_LoadStatistics(FAILURE, 0, 0, error);
        }

        @NonNull
        static LoadStatistics inFlight() {
            return new AutoValue_StatisticsResult_LoadStatistics(IN_FLIGHT, 0, 0,null);
        }

    }
}
