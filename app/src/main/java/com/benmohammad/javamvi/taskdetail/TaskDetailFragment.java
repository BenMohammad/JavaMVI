package com.benmohammad.javamvi.taskdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javamvi.R;
import com.benmohammad.javamvi.mvibase.MviView;
import com.benmohammad.javamvi.util.TodoViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class TaskDetailFragment extends Fragment implements MviView<TaskDetailIntent, TaskDetailViewState> {

    @NonNull
    private static final String ARGUMENT_TASK_ID = "TASK_ID";

    @NonNull
    private static final int REQUEST_EDIT_TASK = 1;

    private TextView detailsTitle;
    private TextView detailsDescription;
    private CheckBox detailsCompleteStatus;

    private FloatingActionButton fab;

    TaskDetailViewModel viewModel;

    private CompositeDisposable disposables = new CompositeDisposable();
    private PublishSubject<TaskDetailIntent.DeleteTask> deleteTaskPublisher = PublishSubject.create();

    public static TaskDetailFragment newInstance(@Nullable String taskId) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TASK_ID, taskId);
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.taskdetail_frag, container, false);
        setHasOptionsMenu(true);
        detailsTitle = root.findViewById(R.id.task_detail_title);
        detailsDescription = root.findViewById(R.id.task_detail_description);
        detailsCompleteStatus = root.findViewById(R.id.task_detail_complete);

        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, TodoViewModelFactory.getInstance(getContext())).get(TaskDetailViewModel.class);
        disposables = new CompositeDisposable();
        bind();
    }

    private void bind() {

    }

    @Override
    public Observable<TaskDetailIntent> intents() {
        return null;
    }

    @Override
    public void render(TaskDetailViewState state) {

    }
}
