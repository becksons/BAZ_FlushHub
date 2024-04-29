package com.example.flushhubproto.schema

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

open class bathroom (
    @PrimaryKey
    var _id: ObjectId = ObjectId(),
    var Coordinates: String = "",
    var Description: String = "",
    var Location:String = "",
    var Name: String = "",
    var Type: String = "",
    var Rating: String = "",
    var Reviews: String = ""
) : RealmObject() {}