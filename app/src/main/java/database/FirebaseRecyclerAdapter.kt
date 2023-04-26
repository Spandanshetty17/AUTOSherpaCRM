package database

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

/**
 * This class is a generic way of backing an RecyclerView with a Firebase location.
 * It handles all of the child events at the given Firebase location. It marshals received data into the given
 * class type.
 *
 * To use this class in your app, subclass it passing in all required parameters and implement the
 * populateViewHolder method.
 *
 * @param T The Java class that maps to the type of objects stored in the Firebase location.
 * @param VH The ViewHolder class that contains the Views in the layout that is shown for each object.
 */
abstract class FirebaseRecyclerAdapter<T, VH : RecyclerView.ViewHolder>(
    private val mModelClass: Class<T>,
    private val mModelLayout: Int,
    private val mViewHolderClass: Class<VH>,
    ref: Query
) : RecyclerView.Adapter<VH>() {

    private val mSnapshots: FirebaseArray = FirebaseArray(ref)

    init {
        mSnapshots.setOnChangedListener(object : FirebaseArray.OnChangedListener {
            override fun onChanged(type: FirebaseArray.EventType, index: Int, oldIndex: Int) {
                when (type) {
                    FirebaseArray.EventType.Added -> notifyItemInserted(index)
                    FirebaseArray.EventType.Changed -> notifyItemChanged(index)
                    FirebaseArray.EventType.Removed -> notifyItemRemoved(index)
                    FirebaseArray.EventType.Moved -> notifyItemMoved(oldIndex, index)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                this@FirebaseRecyclerAdapter.onCancelled(databaseError)
            }
        })
    }

    constructor(modelClass: Class<T>, modelLayout: Int, viewHolderClass: Class<VH>, ref: DatabaseReference)
            : this(modelClass, modelLayout, viewHolderClass, ref as Query)

    override fun getItemCount(): Int = mSnapshots.count

    fun getItem(position: Int): T = parseSnapshot(mSnapshots.getItem(position))

    /**
     * This method parses the DataSnapshot into the requested type. You can override it in subclasses
     * to do custom parsing.
     *
     * @param snapshot the DataSnapshot to extract the model from
     * @return the model extracted from the DataSnapshot
     */
    protected open fun parseSnapshot(snapshot: DataSnapshot): T = snapshot.getValue(mModelClass)!!

    fun getRef(position: Int): DatabaseReference = mSnapshots.getItem(position).ref

    fun cleanup() = mSnapshots.cleanup()

    override fun getItemId(position: Int): Long {
        // http://stackoverflow.com/questions/5100071/whats-the-purpose-of-item-ids-in-android-listview-adapter
        return mSnapshots.getItem(position).key!!.hashCode().toLong()
    }

    /**
     * Called when the adapter is about to be destroyed.
     * It's the right place to remove listeners, cancel queries, etc.
     */
    open fun onCancelled(error: databaseError) {
        Log.w("FirebaseRecyclerAdapter", databaseError.toException())

    }
    protected abstract fun populateViewHolder(viewHolder: VH, model: T, position: Int)

}
