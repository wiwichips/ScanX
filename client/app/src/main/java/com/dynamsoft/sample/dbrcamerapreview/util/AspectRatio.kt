package com.dynamsoft.sample.dbrcamerapreview.util

import android.os.Parcel
import android.os.Parcelable
import android.support.v4.util.SparseArrayCompat

/**
 * Immutable class for describing proportional relationship between width and height.
 */
class AspectRatio private constructor(val x: Int, val y: Int) : Comparable<AspectRatio?>, Parcelable {

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (this === other) {
            return true
        }
        if (other is AspectRatio) {
            return x == other.x && y == other.y
        }
        return false
    }

    override fun toString(): String {
        return "$x:$y"
    }

    fun toFloat(): Float {
        return x.toFloat() / y
    }

    override fun hashCode(): Int {
        // assuming most sizes are <2^16, doing a rotate will give us perfect hashing
        return y xor (x shl Integer.SIZE / 2 or (x ushr Integer.SIZE / 2))
    }

    override operator fun compareTo(other: AspectRatio?): Int {
        if (equals(other)) {
            return 0
        } else if (other != null && toFloat() - other.toFloat() > 0) {
            return 1
        }
        return -1
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(x)
        dest.writeInt(y)
    }

    companion object {

        private val sCache = SparseArrayCompat<SparseArrayCompat<AspectRatio>>(16)

        // This must be present for the class to implement Parcelable
        @JvmField val CREATOR: Parcelable.Creator<AspectRatio?> = object : Parcelable.Creator<AspectRatio?> {
            override fun createFromParcel(source: Parcel): AspectRatio {
                val x = source.readInt()
                val y = source.readInt()
                return of(x, y)
            }

            override fun newArray(size: Int): Array<AspectRatio?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * Returns an instance of [AspectRatio] specified by `x` and `y` values.
         * The values `x` and `` will be reduced by their greatest common divider.
         *
         * @param x The width
         * @param y The height
         * @return An instance of [AspectRatio]
         */
        fun of(x: Int, y: Int): AspectRatio {
            var xVal = x
            var yVal = y
            val gcd = gcd(xVal, yVal)
            xVal /= gcd
            yVal /= gcd
            var arrayX = sCache[xVal]
            return if (arrayX == null) {
                val ratio = AspectRatio(xVal, yVal)
                arrayX = SparseArrayCompat()
                arrayX.put(yVal, ratio)
                sCache.put(xVal, arrayX)
                ratio
            } else {
                var ratio = arrayX[yVal]
                if (ratio == null) {
                    ratio = AspectRatio(xVal, yVal)
                    arrayX.put(yVal, ratio)
                }
                ratio
            }
        }

        private fun gcd(a: Int, b: Int): Int {
            var aVal = a
            var bVal = b
            while (bVal != 0) {
                val c = bVal
                bVal = aVal % bVal
                aVal = c
            }
            return aVal
        }
    }
}