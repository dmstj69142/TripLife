package com.example.triplife

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.triplife.MyApplication.Companion.auth
import com.example.triplife.databinding.ActivityAuthBinding
import com.example.triplife.util.WLog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.*
import kotlin.math.sign

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    private  lateinit var auth: FirebaseAuth

    // 뒤로가기 버튼을 누른 시각을 저장하는 속성
    var initTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.detail.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        auth = Firebase.auth

        SharedManager.init(applicationContext)

        if (MyApplication.checkAuth()) {
            changeVisibility("login")
        } else {
            changeVisibility("logout")
        }

        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            //구글 로그인 결과 처리...........................
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                MyApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            MyApplication.email = account.email
                            changeVisibility("login")
                        } else {
                            changeVisibility("logout")
                        }
                    }
            } catch (e: ApiException) {
                changeVisibility("logout")
            }
        }

        binding.goSignInBtn.setOnClickListener {
            changeVisibility("sign")

            allCheckBtn.setOnClickListener { onCheckChanged(allCheckBtn) }
            firstCheckBtn.setOnClickListener { onCheckChanged(firstCheckBtn) }
            secondCheckBtn.setOnClickListener { onCheckChanged(secondCheckBtn) }
            thirdCheckBtn.setOnClickListener { onCheckChanged(thirdCheckBtn) }
            fourthCheckBtn.setOnClickListener { onCheckChanged(fourthCheckBtn) }
        }

        binding.googleLoginBtn.setOnClickListener {
            //구글 로그인....................
            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            // 구글의 인증 관리 앱 실행
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            requestLauncher.launch(signInIntent)
        }

        binding.signBtn.setOnClickListener {
            //이메일,비밀번호 회원가입........................
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if (task.isSuccessful) {
                        MyApplication.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { sendTask ->
                                if (sendTask.isSuccessful) {
                                    Toast.makeText(
                                        baseContext, "회원가입에서 성공, 전송된 메일을 확인해 주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    changeVisibility("logout")
                                } else {
                                    Toast.makeText(baseContext, "메일 발송 실패", Toast.LENGTH_SHORT)
                                        .show()
                                    changeVisibility("logout")
                                }
                            }
                    } else {
                        Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        changeVisibility("logout")
                    }
                }
        }

        binding.compoundButton.setOnClickListener {
            if (firstCheckBtn.isChecked && secondCheckBtn.isChecked) {
                changeVisibility("signin")
            } else {
                Toast.makeText(baseContext, "이용약관에 동의해주세요.", Toast.LENGTH_SHORT).show()
            }

        }

        binding.loginBtn.setOnClickListener {
            //이메일, 비밀번호 로그인.......................
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){ task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful) {
                        if(MyApplication.checkAuth()) {
                            MyApplication.email = email
                            changeVisibility("login")

                        } else {
                            Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        detail.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("이용약관")
                .setMessage("제1조 (목적)\n" +
                        "본 약관은 TripLife가 제공하는 이용조건, 절차, 이용자와 당 사이트의 권리, 의무, 책임사항과 기타 필요한 사항을 규정하는 것을 목적으로 합니다.\n" +
                        "\n" +
                        "제 2장 서비스이용계약\n" +
                        "제2조 (이용계약의 성립)\n" +
                        "① 만14세 이상이어야 서비스 이용 계약이 가능합니다.\n" +
                        "② 이용계약은 이용자의 이용신청에 대한 서비스의 약관 승락에 의해 성립됩니다.\n" +
                        "③ 제1항의 규정에 의해 이용자가 이용 신청을 할 때에는 정보통신산업진흥원이 이용자 관리시 필요로 하는 사항을 전자적 방식(당 사이트의 서버 등 정보처리장치에 접속하여 데이터를 입력하는 것을 말합니다)이나 서면으로 하여야 합니다.\n" +
                        "④ 이용계약은 이메일 단위로 체결합니다.\n" +
                        "⑤ 서비스의 대량이용 등 특별한 서비스 이용에 관한 계약은 별도의 계약으로 합니다.\n" +
                        "\n" +
                        "제3조 (이용신청)\n" +
                        "① 서비스를 이용하고자 하는 자는 온라인을 통해 당 사이트의 이용신청양식에 따라 요구하는 사항을 기록하여야 합니다.\n" +
                        "② 서비스를 이용하고자 회원으로 가입하기 위해서는 이용신청을 하고 당 사이트 이용자의 개인정보보호지침과 본 약관에 동의를 하여야 합니다.\n" +
                        "③ 회원가입 이후 당 사이트에서 제공하는 서비스를 제공받을 의사가 없는 등의 사유가 있는 경우에는 회원탈퇴(해지) 할 수 있습니다.\n" +
                        "\n" +
                        "제4조 (계약사항의 변경)\n" +
                        "이용자는 다음 사항을 변경하고자 하는 경우 서비스에 접속하여 서비스 내의 기능을 이용하여 변경할 수 있습니다.\n" +
                        "① 개인 이력사항\n" +
                        "② 비밀번호\n" +
                        "③ 기타 정보통신산업진흥원이 인정하는 사항\n" +
                        "\n" +
                        "\n" +
                        "제 3장 서비스의 이용\n" +
                        "제5조 (서비스 이용시간)\n" +
                        "서비스의 이용 시간은 당 사이트의 업무 및 기술상 특별한 지장이 없는 한 연중무휴, 1일 24시간(00:00-24:00)을 원칙으로 합니다. 다만 정기점검 등의 필요로 당 사이트가 정한 날이나 시간은 그러하지 아니합니다.\n" +
                        "\n" +
                        "제 6조(이용자의 정보보안 등)\n" +
                        "① 이용자의 이메일 및 비밀번호에 대한 모든 관리책임은 이용자에게 있습니다.\n" +
                        "② 명백한 사유가 있는 경우를 제외하고는 이용자가 이메일을 공유, 양도 또는 변경할 수 없습니다.\n" +
                        "③ 이용자가 서비스를 이용하기 위해 입력한 이메일에 의하여 발생되는 서비스 이용 상의 과실 또는 제3자에 의한 부정사용 등에 대한 모든 책임은 이용자에게 있습니다.\n" +
                        "\n" +
                        "제 7조 (서비스 이용의 제한 및 이용계약의 해지)\n" +
                        "1. 이용자가 서비스 이용계약을 해지하고자 하는 때에는 온라인으로 해지신청을 하여야 합니다.\n" +
                        "2. 이용자가 다음 각 호에 해당하는 경우 사전통지 없이 이용계약을 해지하거나 전부 또는 일부의 서비스 제공을 중지할 수 있습니다.\n" +
                        "① 타인의 이용자번호를 사용한 경우\n" +
                        "② 다량의 정보를 전송하여 서비스의 안정적 운영을 방해하는 경우\n" +
                        "③ 수신자의 의사에 반하는 광고성 정보, 전자우편을 전송하는 경우\n" +
                        "④ 정보통신설비의 오작동이나 정보 등의 파괴를 유발하는 컴퓨터 바이러스 프로그램등을 유포하는 경우\n" +
                        "⑤ 정보통신윤리위원회로부터의 이용제한 요구 대상인 경우\n" +
                        "⑥ 선거관리위원회의 유권해석 상의 불법선거운동을 하는 경우\n" +
                        "⑦ 서비스를 통해 얻은 정보를 당 사이트의 동의없이 상업적으로 이용하는 경우\n" +
                        "⑧ 기타 당 사이트가 부적당하다고 판단하는 경우\n" +
                        "3. 전항의 규정에 의하여 이용자의 이용을 제한하는 경우와 제한의 종류 및 기간 등 구체적인 기준은 공지, 서비스 이용안내 등에서 별도로 정하는 바에 의합니다.\n" +
                        "\n" +
                        "제 8조 (이용자 게시물의 삭제 및 서비스 이용제한)\n" +
                        "1. 당 사이트는 서비스용 설비의 용량에 여유가 없다고 판단되는 경우 필요에 따라 이용자가 게재 또는 등록한 내용물을 삭제할 수 있습니다.\n" +
                        "2. 당 사이트는 서비스용 설비의 용량에 여유가 없다고 판단되는 경우 이용자의 서비스 이용을 부분적으로 제한할 수 있습니다.\n" +
                        "3. 제 1 항 및 제 2 항의 경우에는 당해 사항을 사전에 온라인을 통해서 공지합니다.\n" +
                        "4. 당 사이트는 이용자가 게재 또는 등록하는 서비스내의 내용물이 다음 각호에 해당한다고 판단되는 경우에 이용자에게 사전 통지없이 삭제할 수 있습니다.\n" +
                        "① 다른 이용자 또는 제 3자를 비방하거나 중상모략으로 명예를 손상시키는 경우\n" +
                        "② 공공질서 및 미풍양속에 위반되는 내용의 정보, 문장, 도형 등을 유포하는 경우\n" +
                        "③ 반국가적, 반사회적, 범죄적 행위와 결부된다고 판단되는 경우\n" +
                        "④ 다른 이용자 또는 제3자의 저작권 등 기타 권리를 침해하는 경우\n" +
                        "⑤ 게시 기간이 규정된 기간을 초과한 경우\n" +
                        "⑥ 이용자의 조작 미숙이나 광고목적으로 동일한 내용의 게시물을 5회이상 반복 등록하였을 경우\n" +
                        "⑦ 기타 관계 법령에 위배된다고 판단되는 경우\n" +
                        "\n" +
                        "제 9조 (서비스 제공의 중지 및 제한)\n" +
                        "1. 당 사이트는 다음 각 호에 해당하는 경우 서비스 제공을 중지할 수 있습니다.\n" +
                        "① 서비스용 설비의 보수 또는 공사로 인한 부득이한 경우\n" +
                        "② 전기통신사업법에 규정된 기간통신사업자가 전기통신 서비스를 중지했을 때\n" +
                        "2. 당 사이트는 국가비상사태, 서비스 설비의 장애 또는 서비스 이용의 폭주 등 으로 서비스 이용에 지장이 있는 때에는 서비스 제공을 중지하거나 제한할 수 있습니다.\n" +
                        "\n" +
                        "제 10조 (개인정보보호)\n" +
                        "1. 당 사이트는 정보통신이용촉진등에 관한 법률등 관계법령에 따라 제공받는 이용자의 개인정보 및 서비스 이용 중 생성되는 개인정보를 보호하여야 합니다.\n" +
                        "2. 당 사이트의 관리책임자는 관리담당 부서장이며, 개인정보 관리책임자의 성명은 별도로 공지하거나 서비스 안내에 게시합니다.\n" +
                        "3. 이용자가 자신의 개인정보를 전송 등의 방법으로 당 사이트에 제공하는 행위는 당 사이트의 개인정보 수집 및 이용 등에 동의하는 것으로 간주되며, 당 사이트의 이용자 개인정보의 수집 및 이용목적은 다음 각 호와 같습니다.\n" +
                        "① 서비스 제공 등 이용계약의 이행\n" +
                        "② 마케팅 정보 생성 및 이용 고객별 안내\n" +
                        "③ 광고 전송 또는 우송\n" +
                        "4. 당 사이트는 개인정보를 이용고객의 별도의 동의 없이 제3자에게 제공하지 않습니다. 다만, 다음 각 호의 경우는 이용고객의 별도 동의 없이 제3자에게 이용고객의 개인정보를 제공할 수있습니다.\n" +
                        "① 수사상의 목적에 따른 수사기관의 서면 요구가 있는 경우에 수사협조의 목적으로 국가수사 기관에 성명, 주소 등 신상정보를 제공하는 경우\n" +
                        "② 신용정보의 이용 및 보호에 관한 법률, 전기통신관련법률 등 법률에 특별한 규정이 있는 경우\n" +
                        "③ 통계, 학술연구 또는 시장조사를 위하여 필요한 경우로서 특정 개인을 식별할 수 없는 형태로 제공하는 경우\n" +
                        "5. 이용자는 자신의 개인정보를 열람할 수 있으며, 오류 수정 등 변경할 수 있습니다. 열람 및 변경은 원칙적으로 이용신청과 동일한 방법으로 하며, 자세한 방법은 당 사이트의 공지사항안내 등의 정한 바에 따릅니다.\n" +
                        "6. 이용자는 이용계약을 해지함으로써 개인정보의 수집 및 이용에 대한 동의, 목적 이외의 사용에 대한 별도 동의, 제3자 제공에 대한 별도 동의를 철회할 수 있으며, 해지방법은 본 약관에서 별도로 규정한 바에 따릅니다.\n" +
                        "\n" +
                        "제 11조 (이용자의 의무)\n" +
                        "1. 이용자는 서비스를 이용할 때 다음 각 호의 행위를 하지 않아야 합니다.\n" +
                        "① 다른 이용자의 이메일을 부정하게 사용하는 행위\n" +
                        "② 서비스를 이용하여 얻은 정보를 당 사이트의 사전승낙 없이 이용자의 이용 이외의 목적으로 복제하거나 이를 출판, 방송 등에 사용하거나 제3자에게 제공하는 행위\n" +
                        "③ 다른 이용자 또는 제3자를 비방하거나 중상모략으로 명예를 손상하는 행위\n" +
                        "④ 공공질서 및 미풍양속에 위배되는 내용의 정보, 문장, 도형 등을 타인에게 유포하는 행위\n" +
                        "⑤ 반국가적, 반사회적, 범죄적 행위와 결부된다고 판단되는 행위\n" +
                        "⑥ 다른 이용자 또는 제3자의 저작권등 기타 권리를 침해하는 행위\n" +
                        "⑦ 기타 관계 법령에 위배되는 행위\n" +
                        "2. 이용자는 이 약관에서 규정하는 사항과 서비스 이용안내 또는 주의사항을 준수하여야 합니다.\n" +
                        "3. 이용자가 설치하는 단말기 등은 전기통신설비의 기술기준에 관한 규칙이 정하는 기준에 적합하여야 하며, 서비스에 장애를 주지 않아야 합니다.\n" +
                        "\n" +
                        " \n" +
                        "제 4장 서비스이용요금\n" +
                        "제12조 (이용요금)\n" +
                        "당 사이트의 서비스 이용료는 무료로 합니다. 단, 당 사이트의 정책에 따라 이용 요금이 유료화될 경우에는 당 사이트의 공지사항안내 등을 통해 게시합니다.\n" +
                        "\n" +
                        "제 5장 저작권\n" +
                        "제13조 (게재된 자료에 대한 권리)\n" +
                        "당 사이트에 게재된 자료에 대한 권리는 다음 각 호와 같습니다.\n" +
                        "① 게시물에 대한 권리와 책임은 게시자에게 있으며, 당 사이트는 게시자의 동의없이는 이를 영리적 목적으로 사용할 수 없습니다. 단, 비영리적 목적인 경우, 당 사이트는 게시자의 동의 없이도 이를 사용할 수 있으며 서비스내의 게재권을 갖습니다.\n" +
                        "② 게시자의 사전 동의가 없이는 이용자는 서비스를 이용하여 얻은 정보를 가공, 판매하는행위 등 서비스에 게재된 자료를 상업적 목적으로 이용할 수 없습니다.\n" +
                        "\n" +
                        "제 6장 이의 신청 및 손해배상 청구 금지\n" +
                        "제14조 (이의신청금지)\n" +
                        "이용자는 당 사이트에서 제공하는 서비스 이용 시 발생되는 어떠한 문제에 대해서도 무료 이용 기간 동안은 이의 신청 및 민원을 제기할 수 없습니다.\n" +
                        "\n" +
                        "제15조 (손해배상청구금지)\n" +
                        "이용자는 당 사이트에서 제공하는 서비스 이용 시 발생되는 어떠한 문제에 대해서도 무료 이용 기간 동안은 정보통신산업진흥원 및 관계 기관에 손해배상 청구를 할 수 없으며 정보통신산업진흥원은 이에 대해 책임을 지지 아니합니다.")
                .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, id ->

                })
            builder.show()
        }
    }

    private fun onCheckChanged(compoundButton: CompoundButton) {
        when(compoundButton.id) {
            R.id.allCheckBtn -> {
                if (allCheckBtn.isChecked) {
                    firstCheckBtn.isChecked = true
                    secondCheckBtn.isChecked = true
                    thirdCheckBtn.isChecked = true
                    fourthCheckBtn.isChecked = true
                }else {
                    firstCheckBtn.isChecked = false
                    secondCheckBtn.isChecked = false
                    thirdCheckBtn.isChecked = false
                    fourthCheckBtn.isChecked = false
                }
            }
            else -> {
                allCheckBtn.isChecked = (
                        firstCheckBtn.isChecked
                                && secondCheckBtn.isChecked
                                && thirdCheckBtn.isChecked
                                && fourthCheckBtn.isChecked)
            }
        }
    }

    fun changeVisibility(mode: String) {
        if (mode === "login") {
            startActivity(Intent(this, AfterloginActivity::class.java))
        } else if (mode === "logout") {
            binding.run {
                authMainTextView.visibility = View.VISIBLE
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
                back.visibility = View.GONE
                check.visibility = View.GONE
                detail.visibility = View.GONE
                allCheckBtn.visibility = View.GONE
                firstCheckBtn.visibility = View.GONE
                secondCheckBtn.visibility = View.GONE
                thirdCheckBtn.visibility = View.GONE
                fourthCheckBtn.visibility = View.GONE
                compoundButton.visibility = View.GONE
            }
        } else if (mode === "signin") {
            binding.run {
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.VISIBLE
                loginBtn.visibility = View.GONE
                back.visibility = View.VISIBLE
                check.visibility = View.GONE
                detail.visibility = View.GONE
                allCheckBtn.visibility = View.GONE
                firstCheckBtn.visibility = View.GONE
                secondCheckBtn.visibility = View.GONE
                thirdCheckBtn.visibility = View.GONE
                fourthCheckBtn.visibility = View.GONE
                compoundButton.visibility = View.GONE
                back.setOnClickListener {
                    changeVisibility("sign")
                }
            }
        } else if (mode == "sign") {
            binding.run {
                authMainTextView.visibility = View.GONE
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.GONE
                googleLoginBtn.visibility = View.GONE
                authEmailEditView.visibility = View.GONE
                authPasswordEditView.visibility = View.GONE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.GONE
                back.visibility = View.VISIBLE
                check.visibility = View.VISIBLE
                check.text = "이 용 약 관"
                detail.visibility = View.VISIBLE
                allCheckBtn.visibility = View.VISIBLE
                firstCheckBtn.visibility = View.VISIBLE
                secondCheckBtn.visibility = View.VISIBLE
                thirdCheckBtn.visibility = View.VISIBLE
                fourthCheckBtn.visibility = View.VISIBLE
                compoundButton.visibility = View.VISIBLE
                back.setOnClickListener {
                    changeVisibility("logout")
                }
            }

        }
    }

    /*
    // 뒤로가기 버튼 이벤트 핸들러
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 뒤로가기 버튼을 눌렀을 때 처리
        if (keyCode === KeyEvent.KEYCODE_BACK) {
            // 뒤로가기 버튼을 처음 눌렀거나 누른지 3초가 지났을 때 처리
            if (System.currentTimeMillis() - initTime > 3000) {
                Toast.makeText(this, "종료하려면 한 번 더 누르세요!", Toast.LENGTH_SHORT).show()
                initTime = System.currentTimeMillis()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

     */
}