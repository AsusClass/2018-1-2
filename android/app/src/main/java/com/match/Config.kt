package com.match

import android.os.Environment

object Config {
    internal var ROOT_PATH = Environment.getExternalStorageDirectory().path + "/match"
}
