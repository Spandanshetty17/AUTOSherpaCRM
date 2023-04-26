package database

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class FirebaseListAdapter<T>(
    private val mActivity: Activity,
    private val mModelClass: Class<T>,
    private val mLayout: Int,
    ref: Query
) : BaseAdapter() {

    private val mSnapshots: FirebaseArray = FirebaseArray(ref)

    init {
        mSnapshots.setOnChangedListener(object : FirebaseArray.OnChangedListener {
            override fun onChanged(type: FirebaseArray.EventType, index: Int, oldIndex: Int) {
                notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                this@FirebaseListAdapter.onCancelled(databaseError)
            }
        })
    }

    constructor(activity: Activity, modelClass: Class<T>, modelLayout: Int, ref: DatabaseReference)
            : this(activity, modelClass, modelLayout, ref as Query)

    fun cleanup() {
        mSnapshots.cleanup()
    }

    override fun getCount(): Int {
        return mSnapshots.count
    }

    override fun getItem(position: Int): T {
        return parseSnapshot(mSnapshots.getItem(position))
    }

    override fun getItemId(i: Int): Long {
        return mSnapshots.getItem(i).key.hashCode().toLong()
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        var convertView = view
        if (convertView == null) {
            convertView = mActivity.layoutInflater.inflate(mLayout, viewGroup, false)
        }

        val model = getItem(position)

        populateView(convertView, model, position)

        return convertView
    }

    protected fun parseSnapshot(snapshot: DataSnapshot): T {
        return snapshot.getValue(mModelClass)!!
    }

    protected fun onCancelled(databaseError: DatabaseError) {
        Log.w("FirebaseRecyclerAdapter", databaseError.toException())
    }

    protected fun getRef(position: Int): DatabaseReference {
        return mSnapshots.getItem(position).ref
    }

    protected abstract fun populateView(view: View, model: T, position: Int)
}