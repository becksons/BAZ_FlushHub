package com.example.flushhubproto.ui.entertainment.fhboggle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tomtom.R
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

class FlushHubBoggle:Fragment(){
    private lateinit var buttons: Array<Array<Button>>
    private var firstSelection = true
    private var lastSelectedRow: Int = -1
    private var lastSelectedCol: Int = -1
    private var wordDisplay : TextView?  = null
    private var backButton : Button? = null
    private var wordBuilder = StringBuilder()
    private var clearButton :Button?  = null
    private var submitButton: Button? = null
    private lateinit var boggleViewModel: BoggleViewModel
    private var isValid = false

    private var newGameButton: Button? = null
    private var scoreCount: TextView? = null
    private var seenWords = mutableListOf<String>()
    private var seenChars = mutableListOf<Char>()
    private var newGameSelected  = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fh_boggle, container, false)
        initializeGameBoard(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wordDisplay = view.findViewById(R.id.word_display)
        clearButton = view.findViewById(R.id.clear_button)
        submitButton = view.findViewById(R.id.submit_button)
        backButton = view.findViewById(R.id.boggle_back_button)
        newGameButton = view.findViewById(R.id.new_game_button)


        boggleViewModel = ViewModelProvider(requireActivity())[BoggleViewModel::class.java]

        scoreCount = view.findViewById(R.id.score_count)
        boggleViewModel.score.observe(viewLifecycleOwner) { newScore ->
            scoreCount?.text = newScore.toString()
        }

        boggleViewModel.resetGameEvent.observe(viewLifecycleOwner) { reset ->
            if (reset) {

                boggleViewModel.resetGameEvent.postValue(false)



            }
        }
        boggleViewModel.submittedWord.observe(viewLifecycleOwner) { word ->
            Log.d("ScoreFragment", "Submitted word: $word")
            if (!seenWords.contains(word) && word.isNotEmpty()) {
                checkIfWordIsValid(word) { isValid ->
                    activity?.runOnUiThread {
                        if (isValid) {
                            seenWords.add(word)
                            calculateAndUpdateScore(word)
                        } else {
                            Toast.makeText(context, "The word $word is not a valid word", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                if (word.isEmpty()) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Submit a word", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Word already used", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        newGameButton?.setOnClickListener {
            findNavController().navigate(R.id.flushHubBoggle)
            resetGame()
            boggleViewModel.score.postValue(0)

        }

        clearButton?.setOnClickListener {
            clearBoard()
        }

        submitButton?.setOnClickListener {
            val word = wordDisplay?.text.toString()
            if (word.isEmpty()) {
                Toast.makeText(context, "Please enter a word.", Toast.LENGTH_SHORT).show()
            } else {
                checkIfWordIsValid(word) { isValid ->
                    activity?.runOnUiThread {
                        if (isValid) {
                            if (!seenWords.contains(word)) {
                                seenWords.add(word)
                                calculateAndUpdateScore(word)
                            } else {
                                Toast.makeText(context, "Word already used", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "The word $word is not a valid word", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            clearBoard()
            Log.d("GameBoardFragment", "Word Submitted: $word")
        }
        backButton?.setOnClickListener {
            findNavController().navigate(R.id.nav_entertainment)
        }
    }
    fun checkIfWordIsValid(word: String, callback: (Boolean) -> Unit) {
        Thread {
            var urlConnection: HttpURLConnection? = null
            try {
                val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$word")
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    callback(true)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            } finally {
                urlConnection?.disconnect()
            }
        }.start()
    }

    private fun calculateAndUpdateScore(word: String) {
        val (calculatedScore, hasEnoughVowels) = calculateScore(word)
        var scoreChange = 0
        val currentScore = boggleViewModel.score.value
        boggleViewModel.score.postValue(currentScore)



        if (!hasEnoughVowels) {
            activity?.runOnUiThread {
                Toast.makeText(context, "Word must have at least 1 vowel", Toast.LENGTH_SHORT).show()
            }
            boggleViewModel.updateScore(-10)
            scoreChange = -10
            activity?.runOnUiThread {
                Toast.makeText(context, " $scoreChange \n Score:$calculatedScore", Toast.LENGTH_SHORT).show()
            }
        } else {
            scoreChange = calculatedScore
            val updatedScore = max(scoreChange + currentScore!!, 0)
            scoreChange = calculatedScore


            activity?.runOnUiThread {
                Toast.makeText(context, " +$scoreChange \n Score:$updatedScore", Toast.LENGTH_SHORT).show()
            }
            boggleViewModel.updateScore(calculatedScore)
        }
    }

    private fun calculateScore(word: String): Pair<Int, Boolean> {
        val vowels = listOf('A', 'E', 'I', 'O', 'U')
        val specialConsonants = listOf('S', 'Z', 'P', 'X', 'Q')
        val vowelCount = word.count { it.uppercaseChar() in vowels }
        if(word.length>=3){
            var wordScore = word.uppercase().fold(0) { acc, c ->
                when {
                    vowels.contains(c) -> acc + 5
                    else -> acc + 1
                }
            }
            if (word.any { it.uppercaseChar() in specialConsonants }) wordScore *= 2


            return Pair(wordScore, vowelCount >= 1)

        }else{
            val scoreDed = boggleViewModel.score.value!! -10
            activity?.runOnUiThread {
                Toast.makeText(context, "Word must be at least 3 characters", Toast.LENGTH_SHORT).show()
            }

            return Pair(scoreDed,vowelCount >= 1)

        }


    }


    private fun resetGame() {
        Log.d("GameBoardFragment", "Game resetting...")
        wordBuilder.clear()
        wordDisplay?.text = getString(R.string.word)
        firstSelection = true
        buttons.forEach { row-> row.forEach { col->
            col.isEnabled= true
        } }
        boggleViewModel.resetGameEvent
        initializeGameBoard(requireView())
        boggleViewModel.resetGameEvent.value = false
    }
    private fun clearBoard(){
        wordDisplay?.text = ""
        wordBuilder.clear()
        buttons.forEach { row->
            row.forEach {col->
                col.isEnabled = true
                firstSelection = true
            }
        }
    }
    private fun initializeGameBoard(view: View) {
        val vowels = listOf('A', 'E', 'I', 'O', 'U')
        val consonants = ('A'..'Z').toList() - vowels
        val totalLetters = 16
        val numberOfVowels = 2

        val lettersForBoard = mutableListOf<Char>().apply {
            repeat(numberOfVowels) {
                add(vowels.random())
            }
            repeat(totalLetters - numberOfVowels) {
                add((vowels + consonants).random())
            }
        }.shuffled()


        val buttonIds = arrayOf(
            arrayOf(R.id.Button00, R.id.Button01, R.id.Button02, R.id.Button03),
            arrayOf(R.id.Button10, R.id.Button11, R.id.Button12, R.id.Button13),
            arrayOf(R.id.Button20, R.id.Button21, R.id.Button22, R.id.Button23),
            arrayOf(R.id.Button30, R.id.Button31, R.id.Button32, R.id.Button33)
        )

        buttons = Array(4) { row ->
            Array(4) { col ->
                view.findViewById<Button>(buttonIds[row][col]).apply {
                    val letterIndex = row * 4 + col
                    text = lettersForBoard[letterIndex].toString()
                    setOnClickListener {
                        onButtonSelected(row, col, it as Button)
                    }
                }
            }
        }
    }


    private fun onButtonSelected(row: Int, col: Int, button: Button) {
        if (firstSelection || isAdjacent(row, col) ) {
            button.isEnabled = false
            firstSelection = false
            lastSelectedRow = row
            lastSelectedCol = col
            wordBuilder.append(button.text.toString())
            wordDisplay?.text = wordBuilder.toString()


        }
    }

    private fun isAdjacent(row: Int, col: Int): Boolean {
        return (row in (lastSelectedRow - 1)..(lastSelectedRow + 1)) && (col in (lastSelectedCol - 1)..(lastSelectedCol + 1))
    }
}