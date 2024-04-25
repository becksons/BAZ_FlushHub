package com.example.flushhubproto.ui.rating

import android.os.Bundle
import androidx.fragment.app.Fragment

class SubmitReviewFragment: Fragment() {
    companion object {
        private const val ARG_RESTROOM_ID = "restroom_id"

        fun newInstance(restroomId: Int) =
            SubmitReviewFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RESTROOM_ID, restroomId)
                }
            }
    }
}