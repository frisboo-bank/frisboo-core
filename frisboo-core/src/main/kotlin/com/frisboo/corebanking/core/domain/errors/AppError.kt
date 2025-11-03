package com.frisboo.corebanking.core.domain.errors

public interface AppError

public data class GenericAppError(
    val message: String,
    val cause: Throwable? = null,
    val exceptionClazz: Class<*>? = null,
) : AppError

public data class InvalidVersion(val msg: String) : AppError
public data class LowerEventVersionError(val id: Any?, val expectedVersion: Any, val eventVersion: Any) : AppError
public data class SameEventVersionError(val id: Any?, val expectedVersion: Any, val eventVersion: Any) : AppError
public data class UpperEventVersionError(val id: Any?, val expectedVersion: Any, val eventVersion: Any) : AppError
public data class InvalidTransactionError(val msg: String, val transactionId: String = "") : AppError
public data class SerializationError(val msg: String) : AppError
