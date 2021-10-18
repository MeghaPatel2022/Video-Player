package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.homeadapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.R;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databinding.ListPresetreverbBinding;

public class PresetReverbAdapter extends RecyclerView.Adapter<PresetReverbAdapter.MyClassView> {

    ArrayList<String> presetReverbList = new ArrayList<>();
    Activity activity;

    public PresetReverbAdapter(ArrayList<String> presetReverbList, Activity activity) {
        this.presetReverbList = presetReverbList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ListPresetreverbBinding presetreverbBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.list_presetreverb, parent, false);

        return new MyClassView(presetreverbBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PresetReverbAdapter.MyClassView holder, int position) {

        holder.presetreverbBinding.tvBane.setSelected(holder.presetreverbBinding.tvBane.isSelected());

        holder.presetreverbBinding.tvBane.setText(presetReverbList.get(position));
        holder.presetreverbBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.presetreverbBinding.tvBane.setSelected(!holder.presetreverbBinding.tvBane.isSelected());

                PresetReverbAdapter.this.notify();
            }
        });
    }

    @Override
    public int getItemCount() {
        return presetReverbList.size();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final ListPresetreverbBinding presetreverbBinding;

        public MyClassView(ListPresetreverbBinding presetreverbBinding) {
            super(presetreverbBinding.getRoot());
            this.presetreverbBinding = presetreverbBinding;
        }
    }
}
