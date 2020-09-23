package com.yde.solvadoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class StepsActivity extends AppCompatActivity {

    ArrayList<String[]> checkStepsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkStepsList = (ArrayList<String[]>) getIntent().getSerializableExtra("CheckSteps");
        setContentView(R.layout.activity_steps);
        RecyclerView mCheckRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mCheckRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mCheckRecyclerView.setLayoutManager(layoutManager);
        CheckAdapter checkAdapter = new CheckAdapter(StepsActivity.this);
        mCheckRecyclerView.setAdapter(checkAdapter);
    }

    private class CheckViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView algorithm;
        private TextView insert;
        private Context context;
        private HashMap<String, Integer> algorithmDetail;

        public CheckViewHolder(@NonNull View itemView) {
            super(itemView);
            ImageButton about = (ImageButton) itemView.findViewById(R.id.about);
            algorithm = (TextView) itemView.findViewById(R.id.algorithm);
            insert = (TextView) itemView.findViewById(R.id.insert);
            algorithmDetail = new HashMap<>();
            algorithmDetail.put("Naked Single", R.string.naked_single);
            algorithmDetail.put("Hidden Single", R.string.hidden_single);
            algorithmDetail.put("Naked Pair", R.string.naked_pair);
            algorithmDetail.put("Pointing Pair", R.string.pointing_pair);
            algorithmDetail.put("Claiming Pair", R.string.claiming_pair);
            algorithmDetail.put("Hidden Pair", R.string.hidden_pair);
            algorithmDetail.put("Naked Triple", R.string.naked_triple);
            algorithmDetail.put("X-Wing", R.string.xWing);
            algorithmDetail.put("Swordfish", R.string.swordfish);
            algorithmDetail.put("Jellyfish", R.string.jellyfish);
            algorithmDetail.put("Finned X-Wing", R.string.finned_x_wing);
            algorithmDetail.put("Brute Force", R.string.brute_force);
            algorithmDetail.put("Finned Swordfish", R.string.finned_swordfish);
            algorithmDetail.put("Finned Jellyfish", R.string.finned_jellyfish);
            algorithmDetail.put("Hidden Quad", R.string.hidden_quad);
            algorithmDetail.put("Hidden Triple", R.string.hidden_triple);
            algorithmDetail.put("Naked Quad", R.string.naked_quad);
            about.setOnClickListener(this);
        }

        @SuppressLint("SetTextI18n")
        public void bind(String[] currentCheckSteps, Context context) {
            algorithm.setText(currentCheckSteps[0]);
            this.context = context;
            if (currentCheckSteps[2].equals(""))
                insert.setText(currentCheckSteps[1]);
            else
                insert.setText(currentCheckSteps[1] + "\n" + currentCheckSteps[2]);
        }

        @Override
        public void onClick(View view) {
            String pure_algorithm = findAlgorithm(algorithm.getText().toString());
            Context designContext = new ContextThemeWrapper(context, R.style.CustomDialog);
            AlertDialog.Builder builder = new AlertDialog.Builder(designContext);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_title, null);
            builder.setCustomTitle(dialogView)
                    .setMessage(algorithmDetail.get(pure_algorithm))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            TextView title = (TextView) dialog.findViewById(R.id.pop);
            TextView content = (TextView) dialog.findViewById(android.R.id.message);
            Typeface face = Typeface.createFromAsset(getAssets(), "fonts/raleway.ttf");
            content.setTypeface(face);
            title.setText(pure_algorithm);
        }

        private String findAlgorithm(String algorithm) {
            String algorithm_name = "";
            int last_substring = -1;
            algorithm = algorithm.trim();
            if (!(algorithm.equals("Naked Single") | algorithm.equals("Brute Force"))) {
                for (int i = 0; i < algorithm.length(); i++) {
                    if (algorithm.charAt(i) == 'i' && last_substring == -1) {
                        last_substring = i;
                    } else if (algorithm.charAt(i) == ' ' && last_substring != -1) {
                        if (algorithm.substring(last_substring, i).equals("in"))
                            algorithm_name = algorithm.substring(0, last_substring - 1);
                        last_substring = -1;
                    }
                }
            } else
                algorithm_name = algorithm;

            return algorithm_name;
        }

    }

    private class CheckAdapter extends RecyclerView.Adapter<CheckViewHolder> {

        Context context;

        public CheckAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public CheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CheckViewHolder(LayoutInflater.from(context).inflate(R.layout.step, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CheckViewHolder holder, int position) {
            holder.bind(checkStepsList.get(position), context);
        }

        @Override
        public int getItemCount() {
            return checkStepsList.size();
        }
    }
}