package database

import com.google.firebase.database.ChildEventListener

import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.DatabaseError

import com.google.firebase.database.Query

import java.util.ArrayList


class FirebaseArray(ref: Query) : ChildEventListener {
    interface OnChangedListener {
        enum class EventType {Added, Changed, Removed, Moved}
        fun onChanged(type: EventType, index: Int, oldIndex: Int = -1)

        fun onCancelled(databaseError: DatabaseError)
    }

    private val mQuery = ref
    private var mListener: OnChangedListener? = null
    private val mSnapshots = ArrayList<DataSnapshot>()

    fun cleanup() {
        mQuery.removeEventListener(this)
    }

    fun getCount(): Int {
        return mSnapshots.size
    }

    fun getItem(index: Int): DataSnapshot {
        return mSnapshots[index]
    }

    private fun getIndexForKey(key: String): Int {
        var index = 0
        for (snapshot in mSnapshots) {
            if (snapshot.key == key) {
                return index
            } else {
                index++
            }
        }
        throw IllegalArgumentException("Key not found")
    }

    // Start of ChildEventListener methods
    override fun onChildAdded(snapshot: DataSnapshot, previousChildKey: String?) {
        var index = 0
        if (previousChildKey != null) {
            index = getIndexForKey(previousChildKey) + 1
        }
        mSnapshots.add(index, snapshot)
        notifyChangedListeners(OnChangedListener.EventType.Added, index)
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildKey: String?) {
        val index = getIndexForKey(snapshot.key!!)
        mSnapshots[index] = snapshot
        notifyChangedListeners(OnChangedListener.EventType.Changed, index)
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
        val index = getIndexForKey(snapshot.key!!)
        mSnapshots.removeAt(index)
        notifyChangedListeners(OnChangedListener.EventType.Removed, index)
    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildKey: String?) {
        val oldIndex = getIndexForKey(snapshot.key!!)
        mSnapshots.removeAt(oldIndex)
        val newIndex = if (previousChildKey == null) 0 else (getIndexForKey(previousChildKey) + 1)
        mSnapshots.add(newIndex, snapshot)
        notifyChangedListeners(OnChangedListener.EventType.Moved, newIndex, oldIndex)
    }

    override fun onCancelled(databaseError: DatabaseError) {
        notifyCancelledListeners(databaseError)
    }
// End of ChildEventListener methods

    fun setOnChangedListener(listener: OnChangedListener) {
        mListener = listener
    }

    protected fun notifyChangedListeners(type: OnChangedListener.EventType, index: Int) {
        notifyChangedListeners(type, index, -1)
    }

    protected fun notifyChangedListeners(type: OnChangedListener.EventType, index: Int, oldIndex: Int) {
        mListener?.onChanged(type, index, oldIndex)
    }

    protected fun notifyCancelledListeners(databaseError: DatabaseError) {
        mListener?.onCancelled(databaseError)
    }
}
}