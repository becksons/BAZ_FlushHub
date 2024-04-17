package com.example.test_2

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

open class test (
    @PrimaryKey
    var _id: ObjectId = ObjectId(),
    var Coordinates: String = "",
    var Description: String = "",
    var Location:String = "",
    var Name: String = "",
    var Type: String = "",
) : RealmObject() {}

