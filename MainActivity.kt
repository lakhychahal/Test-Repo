//Manmohan

package mannu.example.lab2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity(), View.OnClickListener{

    var increment = 0;
    var increment1 = 0
    lateinit var button: Button
    lateinit var button1: Button
    lateinit var button2: Button
    lateinit var tvMsg: TextView
    lateinit var tvMsg1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMsg = findViewById(R.id.textView3)
        tvMsg1 = findViewById(R.id.textView4)
        button = findViewById(R.id.button_increment)
        button1 = findViewById(R.id.button3)
        button2 = findViewById(R.id.button4)
        button2.setOnClickListener(this)

        button.setOnClickListener {
            increment++
            tvMsg.text = increment.toString()
        }
        button1.setOnClickListener{
            increment1++
            tvMsg1.text = increment1.toString()
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.button4-> {
                val intent = Intent(this, VirtualActicity::class.java).apply {
                   putExtra("name", tvMsg.text)
                    putExtra("name1", tvMsg1.text)
                }

                startActivity(intent)
            }
        }
    }
}