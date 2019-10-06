package id.sch.sman1babadanponorogo.smazaba

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, WebViewActivity::class.java)

        guru.setOnClickListener {
            intent.putExtra(EXTRA_URL, "https://app.sman1babadanponorogo.sch.id/auth/login/direktori_guru")
            startActivity(intent)
        }
        presensi.setOnClickListener {
            intent.putExtra(EXTRA_URL, "https://app.sman1babadanponorogo.sch.id/auth/login/presensi_online")
            startActivity(intent)
        }
        simpus.setOnClickListener {
            intent.putExtra(EXTRA_URL, "https://simpus.sman1babadanponorogo.sch.id/frontend")
            startActivity(intent)
        }
        lacak.setOnClickListener {
            intent.putExtra(EXTRA_URL, "https://app.sman1babadanponorogo.sch.id/surat_menyurat/lacak")
            startActivity(intent)
        }
        surat.setOnClickListener {
            intent.putExtra(EXTRA_URL, "https://app.sman1babadanponorogo.sch.id/auth/login/surat_menyurat")
            startActivity(intent)
        }
        inventory.setOnClickListener {
            intent.putExtra(EXTRA_URL, "https://app.sman1babadanponorogo.sch.id/auth/login/inventory")
            startActivity(intent)
        }

    }
}
