package com.benmohammad.javamvi.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javamvi.R;
import com.benmohammad.javamvi.addedittask.AddEditTaskActivity;
import com.benmohammad.javamvi.mvibase.MviView;
import com.benmohammad.javamvi.taskdetail.TaskDetailsActivity;
import com.benmohammad.javamvi.util.TodoViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class TasksFragment extends Fragment implements MviView<TasksIntent, TasksViewState> {

    private TasksAdapter listAdapter;
    private TasksViewModel viewModel;
    private View noTasksView;
    private ImageView noTaskIcon;
    private TextView noTasksMainView;
    private TextView noTasksAddView;
    private LinearLayout tasksView;
    private TextView filteringLabelView;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;
    private PublishSubject<TasksIntent.RefreshIntent> refreshIntentPublisher = PublishSubject.create();
    private PublishSubject<TasksIntent.ClearCompletedTaskIntent> completedTaskIntentPublisher = PublishSubject.create();
    private PublishSubject<TasksIntent.ChangeFilterIntent> changeFilterIntentPublisher = PublishSubject.create();
    private CompositeDisposable disposables = new CompositeDisposable();

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAdapter = new TasksAdapter(new ArrayList<>(0));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, TodoViewModelFactory.getInstance(getContext())).get(TasksViewModel.class);
        bind();
    }

    private void bind(){
        disposables.add(viewModel.states().subscribe(this::render));
        viewModel.processIntents(intents());
        disposables.add(listAdapter.getTaskClickObservable().subscribe(task -> showDetailsUI(task.getId())));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshIntentPublisher.onNext(TasksIntent.RefreshIntent.create(false));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            showSuccessfullySavedMessage();
        }
    }
    private void showMessage(String message) {
        View view = getView();
        if(view == null) return;
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    private void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tasks_frag, container, false);

        ListView listView = root.findViewById(R.id.tasks_list);
        listView.setAdapter(listAdapter);
        filteringLabelView = root.findViewById(R.id.filteringLabel);
        tasksView = root.findViewById(R.id.tasksLL);
        noTasksView = root.findViewById(R.id.noTasks);
        noTaskIcon = root.findViewById(R.id.noTasksIcon);
        noTasksMainView = root.findViewById(R.id.noTasksMain);
        noTasksAddView = root.findViewById(R.id.noTasksAdd);
        noTasksAddView.setOnClickListener(ignored -> showAddTask());

        FloatingActionButton fab = getActivity().findViewById(R.id.fab_add_task);
        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(ignored -> showAddTask());
        swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );

        swipeRefreshLayout.setScrollUpChild(listView);
        setHasOptionsMenu(true);
        return root;
    }



    @Override
    public Observable<TasksIntent> intents() {
        return Observable.merge(initialIntent(), refreshIntent(), adapterIntent(), clearCompletedTaskIntent()).mergeWith(changeFilterIntent());
    }

    private Observable<TasksIntent.InitialIntent> initialIntent() {
        return Observable.just(TasksIntent.InitialIntent.create());
    }

    private Observable<TasksIntent.RefreshIntent> refreshIntent(){
        return  RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
                .map(ignored -> TasksIntent.RefreshIntent.create(false))
                .mergeWith(refreshIntentPublisher);
    }

    private Observable<TasksIntent.ClearCompletedTaskIntent> clearCompletedTaskIntent() {
        return completedTaskIntentPublisher;
    }


    private Observable<TasksIntent> adapterIntent() {
        return listAdapter.getTAskToggleObservable().map(task -> {
            if(!task.isCompleted()) {
                return TasksIntent.CompleteTaskIntent.create(task);
            } else {
                return TasksIntent.ActivateTaskIntent.create(task);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.task_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_clear:
                completedTaskIntentPublisher.onNext(TasksIntent.ClearCompletedTaskIntent.create());
                break;
            case R.id.menu_filter:
                showFilterPopupMenu();
                break;
            case R.id.menu_refresh:
                refreshIntentPublisher.onNext(TasksIntent.RefreshIntent.create(true));
                break;
        }
        return true;
    }

    private void showFilterPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popupMenu.getMenuInflater().inflate(R.menu.filter_tasks, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch(item.getItemId()) {
                case R.id.active:
                    changeFilterIntentPublisher.onNext(TasksIntent.ChangeFilterIntent.create(TaskFilterType.ACTIVE_TASKS));
                    break;
                case R.id.completed:
                    changeFilterIntentPublisher.onNext(TasksIntent.ChangeFilterIntent.create(TaskFilterType.COMPLETED_TASKS));
                    break;
                default:
                    changeFilterIntentPublisher.onNext(TasksIntent.ChangeFilterIntent.create(TaskFilterType.ALL_TASKS));
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private Observable<TasksIntent.ChangeFilterIntent> changeFilterIntent() {
        return changeFilterIntentPublisher;
    }

    private void showLoadTaskError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    @Override
    public void render(TasksViewState state) {
        swipeRefreshLayout.setRefreshing(state.isLoading());
        if(state.error() != null) {
            showLoadTaskError();
            return;
        }
        if(state.taskActivated()) showMessage(getString(R.string.task_marked_active));

        if(state.taskComplete()) showMessage(getString(R.string.task_marked_complete));

        if(state.completedTaskCleared()) showMessage(getString(R.string.completed_tasks_cleared));

        if(state.tasks().isEmpty()) {
            switch(state.taskFilterType()) {
                case ACTIVE_TASKS:
                    showNoActiveTasks();
                    break;
                case COMPLETED_TASKS:
                    showNoCompletedTasks();
                    break;
                default:
                    showNoTAsks();
                    break;
            }
        } else {
            listAdapter.replaceData(state.tasks());
            tasksView.setVisibility(View.VISIBLE);
            noTasksView.setVisibility(View.GONE);

            switch(state.taskFilterType()) {
                case ACTIVE_TASKS:
                    showActiveFilterLabel();
                    break;
                case COMPLETED_TASKS:
                    showCompletedTaskLabel();
                    break;
                default:
                    showAllTasksLabel();
            }
        }
    }

    private void showNoActiveTasks() {
        showNoTasksView(getResources().getString(R.string.no_tasks_active), R.drawable.ic_check_circle_24dp, true);
    }

    private void showNoCompletedTasks() {
        showNoTasksView(getResources().getString(R.string.no_tasks_completed), R.drawable.ic_verified_user_24dp, false);
    }

    private void showNoTAsks() {
        showNoTasksView(getResources().getString(R.string.no_tasks_all), R.drawable.ic_assignment_turned_in_24dp, true);
    }

    private void showNoTasksView(String mainText, int iconRes, boolean showAddView) {
        tasksView.setVisibility(View.GONE);
        noTasksView.setVisibility(View.VISIBLE);
        noTasksMainView.setText(mainText);
        noTaskIcon.setImageDrawable(getResources().getDrawable(iconRes));
        noTasksAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    private void showActiveFilterLabel() {
        filteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    private void showCompletedTaskLabel() {
        filteringLabelView.setText(getResources().getString(R.string.label_completed));
    }

    private void showAllTasksLabel() {
        filteringLabelView.setText(getResources().getString(R.string.label_all));
    }



    private void showDetailsUI(String taskId) {
        Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    private void showAddTask() {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK);
    }
}
