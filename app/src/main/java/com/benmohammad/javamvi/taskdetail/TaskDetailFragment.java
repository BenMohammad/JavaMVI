package com.benmohammad.javamvi.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javamvi.R;
import com.benmohammad.javamvi.addedittask.AddEditTaskActivity;
import com.benmohammad.javamvi.addedittask.AddEditTaskFragment;
import com.benmohammad.javamvi.mvibase.MviView;
import com.benmohammad.javamvi.util.TodoViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

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

        fab = getActivity().findViewById(R.id.fab_edit_task);

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
        disposables.add(viewModel.states().subscribe(this::render));
        viewModel.processIntents(intents());
        RxView.clicks(fab).debounce(200, TimeUnit.MILLISECONDS)
                .subscribe(view -> showEditTask(getArgumentTaskId()));

    }

    private void showEditTask(@NonNull String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    private String getArgumentTaskId() {
        Bundle args = getArguments();
        if(args == null) return null;
        return args.getString(ARGUMENT_TASK_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Override
    public Observable<TaskDetailIntent> intents() {
        return Observable.merge(initialIntent(), checkBoxIntent(), deleteIntent());
    }

    private Observable<TaskDetailIntent.InitialIntent> initialIntent() {
        return Observable.just(TaskDetailIntent.InitialIntent.create(getArgumentTaskId()));
    }

    private Observable<TaskDetailIntent> checkBoxIntent() {
        return RxView.clicks(detailsCompleteStatus).map(
                activated ->{
                    if(detailsCompleteStatus.isChecked()) {
                        return TaskDetailIntent.CompleteTaskIntent.create(getArgumentTaskId());
                    } else {
                        return TaskDetailIntent.ActivateTaskIntent.create(getArgumentTaskId());
                    }
                }
        );
    }

    private Observable<TaskDetailIntent.DeleteTask> deleteIntent() {
        return deleteTaskPublisher;
    }

    @Override
    public void render(TaskDetailViewState state) {
        setLoadingIndicator(state.loading());

        if(!state.title().isEmpty()) {
            showTitle(state.title());
        } else {
            hideTitle();
        }

        if(!state.description().isEmpty()) {
            showDescription(state.description());
        } else {
            hideDescription();
        }

        showActive(state.active());

        if(state.taskComplete()) {
            showTaskMarkedComplete();
        }

        if(state.taskActivated()) {
            showTaskMarkedActive();
        }

        if(state.taskDeleted()) {
            getActivity().finish();
        }
    }

    private void showTaskMarkedActive() {
        Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_SHORT).show();

    }

    private void showTaskMarkedComplete() {
        Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_SHORT).show();
    }

    private void showActive(boolean active) {
        detailsCompleteStatus.setChecked(!active);
    }

    private void hideDescription() {
        detailsDescription.setVisibility(View.GONE);
    }

    private void showDescription(String description) {
        detailsDescription.setVisibility(View.VISIBLE);
        detailsDescription.setText(description);

    }

    private void hideTitle() {
        detailsTitle.setVisibility(View.GONE);
    }

    private void showTitle(String title) {
        detailsTitle.setVisibility(View.VISIBLE);
        detailsTitle.setText(title);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_EDIT_TASK) {
            if(resultCode == Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setLoadingIndicator(boolean isActive) {
        if(isActive) {
            detailsTitle.setText("");
            detailsDescription.setText(getString(R.string.loading));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                deleteTaskPublisher.onNext(TaskDetailIntent.DeleteTask.create(getArgumentTaskId()));
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}
