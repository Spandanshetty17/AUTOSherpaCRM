package entity

class CallTrigerModel {
    var checkForeground:Boolean=false

    fun getCheckForeground(): Boolean? {
        return checkForeground
    }

    fun setCheckForeground(checkForeground: Boolean?) {
        this.checkForeground = checkForeground!!
    }
}
