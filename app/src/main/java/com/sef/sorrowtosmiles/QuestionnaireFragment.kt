package com.sef.sorrowtosmiles

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.sef.sorrowtosmiles.databinding.FragmentQuestionnaireBinding
import org.json.JSONObject
import java.time.LocalDateTime
import kotlin.text.Regex.Companion.escape


/**
 * Questionnaire Fragment (User)
 */
class QuestionnaireFragment : Fragment() {

    companion object{
        const val ANSWER = "answer"
        const val datetime = "datetime"
        const val command_answerQuestionnaire = "answerQuestionnaire"
    }

    private var _binding: FragmentQuestionnaireBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val answer = arrayOf(0, 4, 0, 0)

    /**
     * Default Override
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuestionnaireBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Default Override
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh()
        setupButtons()
    }

    /**
     * Select (main) a single button from a group of buttons (deselect others (sub))
     */
    private fun activateInButtonGroup(buttons: ArrayList<Button>, activeButton: Button){
        for (b in buttons){
            subButton(b)
        }
        mainButton(activeButton)
    }

    /**
     * Make a button not the main selection
     */
    private fun subButton(b: Button){
        b.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_sub_button_background))
        b.setTextColor(resources.getColor(R.color.main_sub_button_text))
        b.typeface = Typeface.DEFAULT_BOLD
        println("Sub Button for " + b.text)
    }

    /**
     * Make a button the main selection
     */
    private fun mainButton(b: Button){
        b.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.main_focal_button_background))
        b.setTextColor(resources.getColor(R.color.main_focal_button_text))
        b.typeface = Typeface.DEFAULT_BOLD
        println("Main Button for " + b.text)
    }

    private fun setupButtons(){
        val u = Login.instance.loggedInUser!!
        val email = u.email
        binding.buttonSubmit.setOnClickListener{
            var str = ""
            for (i in answer){
                str += i.toString()
            }
            val datetimeNow = LocalDateTime.now().toString().substring(0, 19).replace("T", " ")
            val json = JSONObject("{ 'command': '$command_answerQuestionnaire', 'packet': { 'email': '$email', '$ANSWER': '$str', '$datetime': '$datetimeNow' }}")
            URLRequest.jsonPOSTRequest(URLRequest.serverURL, json, {
                Toast.makeText(context, "Successfully Submitted Questionnaire :)", Toast.LENGTH_LONG).show()
                askToTweet()
            },{

            })
        }
    }

    /**
     * Refreshes the questionnaire, considers if is in viewing or answering mode
     */
    private fun refresh(){
        val u = Login.instance.loggedInUser!!

        val buttons0 = arrayListOf(binding.buttonQuestion0Y, binding.buttonQuestion0N)
        val buttons1 = arrayListOf(binding.buttonQuestion11, binding.buttonQuestion12, binding.buttonQuestion13, binding.buttonQuestion14, binding.buttonQuestion15)
        val buttons2 = arrayListOf(binding.buttonQuestion2None, binding.buttonQuestion2Couple, binding.buttonQuestion2Always)
        val buttons3 = arrayListOf(binding.buttonQuestion3Great, binding.buttonQuestion3Fine, binding.buttonQuestion3Optimistic,binding.buttonQuestion3Depressed, binding.buttonQuestion3Angry)
        val greaterButtonGroup = arrayOf(buttons0, buttons1, buttons2, buttons3)

        if (u.isViewingQuestionnaire){
            val txtAns = u.questionnaireAnswer
            val ans = ArrayList<Int>()
            for (i in txtAns.indices){
                ans.add(txtAns[i].digitToInt())
            }
            for (i in ans.indices){
                val a = ans[i]
                val bGroup = greaterButtonGroup[i]
                activateInButtonGroup(bGroup, bGroup[a])
            }
            binding.buttonSubmit.visibility = View.GONE
            return
        }
        for (i in greaterButtonGroup.indices){
            val bGroup = greaterButtonGroup[i]
            for (j in bGroup.indices){
                val b = bGroup[j]
                b.setOnClickListener{
                    answer[i] = j
                    var str = ""
                    for (a in answer){
                        str += a.toString()
                    }
                    activateInButtonGroup(bGroup, b)
                    println(str)
                }
            }
        }


    }

    /**
     * Asks the user if they want to tweet their result
     */
    private fun askToTweet(){

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle("Share your results!")
        builder.setMessage("Tweet out your questionnaire results to share!")
        builder.setPositiveButton("Tweet!"
        ) {
            dialog, _ ->
                val text = getQuestionnaireResultText()
                val link = "https://twitter.com/intent/tweet?text=$text"
                val httpIntent = Intent(Intent.ACTION_VIEW)
                println(link)
                httpIntent.data = Uri.parse(link)
                startActivity(httpIntent)
                dialog.dismiss()
        }
        builder.setNegativeButton(android.R.string.cancel
        ) { dialog, _ -> dialog.dismiss() }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Gets the result text of the answers to tweet
     */
    private fun getQuestionnaireResultText() : String{
        val textQuestionArr = arrayOf(
            "1) Are you fine today? \uD83D\uDE2F",
            "2) From 1 being bad to 5 being good, how is your day?",
            "3) Do you have any negative thoughts?",
            "4) How's life right now for you?"
        )
        val textAnswerArr = arrayOf(
            arrayOf("Yeah ☺️", "No ☹️"),
            arrayOf("1 \uD83D\uDE14", "2 \uD83D\uDE15", "3 \uD83D\uDE36", "4 \uD83D\uDE0A", "5 \uD83D\uDE1D"),
            arrayOf("Nope ☀️", "A few \uD83C\uDF26️", "A lot ⛈️"),
            arrayOf("Never felt any better in my life \uD83D\uDE07", "Just like any other day \uD83E\uDD37\uD83C\uDFFB\u200D♀️", "Things could've been better \uD83D\uDE15", "I wish I was never born \uD83D\uDC23", "I hate everything \uD83D\uDE21")
        )
        var text = ""
        for (i in answer.indices){
            val ans = answer[i]
            text += textQuestionArr[i]
            text += "\n"
            text += textAnswerArr[i][ans]
            text += "\n\n"
        }
        text = text.replace("\n", "%0A")
        return text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}