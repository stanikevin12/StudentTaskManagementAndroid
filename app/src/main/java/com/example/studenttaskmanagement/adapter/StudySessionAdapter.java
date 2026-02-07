package com.example.studenttaskmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studenttaskmanagement.R;
import com.example.studenttaskmanagement.model.StudySession;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying study session history.
 * This class is responsible only for binding session data to item views.
 */
public class StudySessionAdapter extends RecyclerView.Adapter<StudySessionAdapter.StudySessionViewHolder> {

    private final List<StudySession> sessions;

    public StudySessionAdapter(List<StudySession> sessions) {
        this.sessions = sessions != null ? sessions : new ArrayList<>();
    }

    @NonNull
    @Override
    public StudySessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_study_session, parent, false);
        return new StudySessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudySessionViewHolder holder, int position) {
        StudySession session = sessions.get(position);

        holder.textViewStartTime.setText(formatDateTime(session.getStartTime()));
        holder.textViewEndTime.setText(session.getEndTime() > 0
                ? formatDateTime(session.getEndTime())
                : "In progress");
        holder.textViewDuration.setText(formatDurationMinutes(session.getDuration()));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void setSessions(List<StudySession> updatedSessions) {
        sessions.clear();
        if (updatedSessions != null) {
            sessions.addAll(updatedSessions);
        }
        notifyDataSetChanged();
    }

    private String formatDateTime(long timeMillis) {
        if (timeMillis <= 0L) {
            return "-";
        }
        return DateFormat.getDateTimeInstance().format(new Date(timeMillis));
    }

    private String formatDurationMinutes(long durationMillis) {
        long minutes = Math.max(0L, durationMillis / 60000L);
        return String.format(Locale.getDefault(), "%d min", minutes);
    }

    static class StudySessionViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewStartTime;
        final TextView textViewEndTime;
        final TextView textViewDuration;

        StudySessionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStartTime = itemView.findViewById(R.id.textViewSessionStartTime);
            textViewEndTime = itemView.findViewById(R.id.textViewSessionEndTime);
            textViewDuration = itemView.findViewById(R.id.textViewSessionDuration);
        }
    }
}
