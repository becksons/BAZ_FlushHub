package com.example.flushhubproto.schema

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId

open class bathroom (
    @PrimaryKey
    var _id: ObjectId = ObjectId(),
    var Coordinates: String = "", //coordinates of the bathrooms
    var Description: String = "", //Instruction to get to the bathroom
    var Location:String = "", //Street address
    var Name: String = "", //Name of the location
    var Type: String = "", //Gender
    var Rating: Double = 0.0, //Rating in stars
    var Reviews: String = "" //user reviews on the bathroom
) : RealmObject() {}