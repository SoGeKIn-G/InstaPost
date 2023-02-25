import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gauravbora.instagrampost.R
import com.gauravbora.instagrampost.modal.Post

class PostAdapter(private val context: Context,private val posts : ArrayList<Post>):RecyclerView.Adapter<PostAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.post,parent,false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
//        Glide.with(context).load(posts[position].url).into(holder.postImage)
        var data: String? =posts[position].url

        if (data != null) {
            if(data.contains("videos")){
                holder.postImage.visibility=View.GONE
                holder.postVideo.visibility=View.VISIBLE

//                    Toast.makeText(context,"${ holder.postImage.visibility}",Toast.LENGTH_SHORT).show()

                val videoUri = Uri.parse(data)
                holder.postVideo.setVideoURI(videoUri)
                holder.postVideo.setOnPreparedListener { mp ->
                    Toast.makeText(context,"looping",Toast.LENGTH_SHORT).show()
                    mp.isLooping = true
                    mp.start()
                }

            }
            else if(data.contains("image")){
                holder.postVideo.visibility=View.GONE
                holder.postImage.visibility=View.VISIBLE
                Glide.with(context).load(data).into(holder.postImage)
            }
        }
    }


    override fun getItemCount(): Int {
//        Toast.makeText(context,"${posts.size}",Toast.LENGTH_SHORT).show()
        return posts.size
    }


    class ImageViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val postImage=itemView.findViewById<ImageView>(R.id.postImage)
        val postVideo=itemView.findViewById<VideoView>(R.id.postVideo)
    }
}