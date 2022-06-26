package org.techtown.gtguildraid.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import org.techtown.gtguildraid.R
import org.techtown.gtguildraid.adapters.DialogImageSpinnerAdapter
import org.techtown.gtguildraid.databinding.ActivityBossEditBinding
import org.techtown.gtguildraid.etc.BossBottomSheetDialog
import org.techtown.gtguildraid.models.BossImage
import org.techtown.gtguildraid.models.entities.Boss
import org.techtown.gtguildraid.utils.RoomDB

class BossEditActivity : AppCompatActivity(), BossBottomSheetDialog.BottomSheetListener {
    private lateinit var binding : ActivityBossEditBinding

    private lateinit var bossImage: ImageView
    private lateinit var bossName: EditText
    private lateinit var boss: Boss

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBossEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bossImage = binding.bossImage
        bossName = binding.bossName
        val elementSpinner = binding.elementSpinner
        val hardness = binding.hardness
        val button = binding.button
        val hardnessValue = binding.hardnessValue

        title = "보스 편집"
        val database = RoomDB.getInstance(this)
        val position = intent.getIntExtra("position", 0)
        val bossId = intent.getIntExtra("bossId", 0)
        boss = database.bossDao().getBoss(bossId)

        val toast = Toast.makeText(applicationContext, null, Toast.LENGTH_SHORT)

        // spinner 설정
        setElementSpinner(elementSpinner)
        elementSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                boss.elementId = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        bossImage.setImageResource(getIdentifierFromResource("boss_" + boss.imgName))
        bossName.setText(boss.name)
        elementSpinner.setSelection(boss.elementId)
        hardness.progress = (boss.hardness * 10).toInt()
        hardnessValue.text = "x " + boss.getHardness()


        //boss 이미지 창 띄우고, 누른 것에 따라 이미지 및 이름 설정
        bossImage.setOnClickListener {
            val bottomSheetDialog = BossBottomSheetDialog(this)
            bottomSheetDialog.show(supportFragmentManager, "bottomSheetDialog")
        }
        hardness.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                if (progress < 1) {
                    hardness.progress = 1
                    hardnessValue.text = "x " + 0.1
                } else hardnessValue.text = "x " + progress / 10.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        //업데이트
        button.setOnClickListener {
            val sName = bossName.text.toString().trim { it <= ' ' }
            val dHardness = hardness.progress / 10.0
            val imgName = boss.imgName as String
            val elementIdx = elementSpinner.selectedItemPosition
            if (elementIdx == 0 || sName == "") {
                toast.setText("입력 부족, 다시 입력해주세요.")
                toast.show()
                return@setOnClickListener
            }
            database.raidDao().updateBoss(bossId, sName, imgName, dHardness, elementIdx, false)
            val retIntent = Intent()
            retIntent.putExtra("position", position)
            setResult(RESULT_OK, retIntent)
            finish()
        }
    }

    private fun setElementSpinner(elementSpinner: Spinner) {
        val elementKoreanArray = listOf("선택", "화", "수", "지", "광", "암", "무")
        val elementEnglishArray = arrayOf("", "fire", "water", "earth", "light", "dark", "basic")
        val elementImageList: MutableList<Int> = ArrayList()
        for (element in elementEnglishArray) {
            val imageId = resources.getIdentifier(
                "element_$element", "drawable", packageName
            )
            elementImageList.add(imageId)
        }
        elementSpinner.adapter = DialogImageSpinnerAdapter(
            applicationContext,
            R.layout.spinner_value_layout,
            elementKoreanArray,
            elementImageList
        )
    }

    private fun getIdentifierFromResource(name: String): Int {
        return resources.getIdentifier(
            name, "drawable", packageName
        )
    }

    override fun onImageClicked(bi: BossImage) {
        bossImage.setImageResource(bi.imgId)
        boss.imgName = bi.imgName
        bossName.setText(bi.bossName)
    }
}