package com.telemedicine.telecare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.telemedicine.telecare.R;
import com.telemedicine.telecare.fragment.PhotoViewFragment;
import com.telemedicine.telecare.model.other.File;
import com.telemedicine.telecare.util.LocalStorage;
import com.telemedicine.telecare.util.enums.Role;

import java.util.ArrayList;
import java.util.List;
import static com.telemedicine.telecare.util.extensions.AppExtensions.loadPhoto;

public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<File>              files = new ArrayList<>();
    private OnFileSelectListener    mOnFileSelectListener;
    private int                     viewType = -1;

    public FilesAdapter() {}

    public FilesAdapter(int viewType) {
        this.viewType = viewType;
    }

    public void setFiles(List<File> files) {
        this.files = files == null || files.isEmpty() ? new ArrayList<>() : files;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType == -1 ? (files.size() > 1 ? (files.size() > 4 ? 2 : 1) : 0) : viewType;
    }

    private File getFile(int position){
        return files.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            default:
            case 0: return new PhotoViewHolder(layoutInflater.inflate(R.layout.sample_photo, parent, false));
            case 1: return new PhotoViewHolder(layoutInflater.inflate(R.layout.sample_photo_grid_2, parent, false));
            case 2: return new PhotoViewHolder(layoutInflater.inflate(R.layout.sample_photo_grid_3, parent, false));
            case 3: return new ReportViewHolder(layoutInflater.inflate(R.layout.sample_user_report, parent, false));
        }
    }

    private static class PhotoViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageView  photoHolder;

        private PhotoViewHolder(View v) {
            super(v);
            photoHolder = v.findViewById(R.id.photo_Holder_Iv);
        }
    }

    private static class ReportViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageView report_Photo;
        private final AppCompatTextView  report_Title;

        private ReportViewHolder(View v) {
            super(v);
            report_Photo = v.findViewById(R.id.report_Photo_Iv);
            report_Title = v.findViewById(R.id.report_Title_Tv);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final File file = getFile(position);
        if(viewHolder instanceof PhotoViewHolder){
            PhotoViewHolder holder = (PhotoViewHolder) viewHolder;

            switch (getItemViewType(position)){
                default:
                case 0: loadPhoto(holder.photoHolder, file.getPath(), null, R.drawable.ic_placeholder_large); break;
                case 1: loadPhoto(holder.photoHolder, file.getPath(), R.dimen._114sdp, R.drawable.ic_placeholder_large); break;
                case 2: loadPhoto(holder.photoHolder, file.getPath(), R.dimen._76sdp, R.drawable.ic_placeholder_large); break;
            }

            holder.itemView.setOnClickListener(view -> {
                PhotoViewFragment.show(file.getPath());
                if(mOnFileSelectListener != null) mOnFileSelectListener.onClick(file);
            });
        }
        else if(viewHolder instanceof ReportViewHolder){
            ReportViewHolder holder = (ReportViewHolder) viewHolder;
            loadPhoto(holder.report_Photo, file.getPath(), R.dimen._156sdp, R.drawable.ic_placeholder_large);
            holder.report_Title.setVisibility(file.getTitle() == null ? View.INVISIBLE : View.VISIBLE);
            holder.report_Title.setText(file.getTitle());

            holder.itemView.setOnClickListener(view -> {
                if(LocalStorage.USER.getRole().equals(Role.DOCTOR.getId())) PhotoViewFragment.show(file.getPath());
            });

            holder.itemView.setOnLongClickListener(view -> {
                if(mOnFileSelectListener != null) mOnFileSelectListener.onClick(file);
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void setOnFileListener(OnFileSelectListener onFileSelectListener) {
        this.mOnFileSelectListener = onFileSelectListener;
    }

    public interface OnFileSelectListener {
        void onClick(File file);
    }
}
