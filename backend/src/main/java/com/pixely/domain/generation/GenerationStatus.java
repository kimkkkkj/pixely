package com.pixely.domain.generation;

public enum GenerationStatus {
    PENDING, PROCESSING, DONE, FAILED;

    public boolean isFinished() {
        return this == DONE || this == FAILED;
    }
}
