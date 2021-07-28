package com.sksulai.checksite.db.converter

interface IConverter<Type, DBType> {
    fun from(value: Type): DBType
    fun to(value: DBType): Type
}
