import android.graphics.BitmapFactory
import android.util.Base64

fun base64ToBitmap(base64: String): android.graphics.Bitmap {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
