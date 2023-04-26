package retrofitinterface

import com.squareup.otto.Bus


class BusProvider {
    private val BUS=Bus()

    fun getInstance():Bus
    {
        return BUS
    }
}