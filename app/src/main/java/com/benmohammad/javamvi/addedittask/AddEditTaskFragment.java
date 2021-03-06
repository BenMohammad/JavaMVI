package com.benmohammad.javamvi.addedittask;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javamvi.R;
import com.benmohammad.javamvi.mvibase.MviView;
import com.benmohammad.javamvi.util.TodoViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.view.RxView;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;

public class AddEditTaskFragment extends Fragment implements MviView<AddEditTaskIntent, AddEditTaskViewState> {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";
    private TextView title;
    private TextView description;
    private FloatingActionButton fab;
    private AddEditTaskViewModel viewModel;
    private CompositeDisposable disposables = new CompositeDisposable();


    public static AddEditTaskFragment newInstance() {
        return new AddEditTaskFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.addtask_frag, container, false);
        title = root.findViewById(R.id.add_task_title);
        description = root.findViewById(R.id.add_task_description);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        viewModel = new ViewModelProvider(this, TodoViewModelFactory.getInstance(getContext())).get(AddEditTaskViewModel.class);
        disposables = new CompositeDisposable();
        bind();
    }

    private void bind() {
        disposables.add(viewModel.states().subscribe(this::render));
        viewModel.processIntents(intents());
    }

    private String getArgumentsTaskId() {
        Bundle args = getArguments();
        if(args == null) return null;
        return args.getString(ARGUMENT_EDIT_TASK_ID);
    }

    @Override
    public Observable<AddEditTaskIntent> intents() {
        return Observable.merge(initialIntent(), saveTaskIntent());
    }

    private Observable<AddEditTaskIntent.InitialIntent> initialIntent() {
        return Observable.just(AddEditTaskIntent.InitialIntent.create(getArgumentsTaskId()));
    }

    private Observable<AddEditTaskIntent.SaveTask> saveTaskIntent() {
        return RxView.clicks(fab).map(ignored ->
                AddEditTaskIntent.SaveTask.create(
                        getArgumentsTaskId(),
                        title.getText().toString(),
                        description.getText().toString()
                ));
    }

    @Override
    public void render(AddEditTaskViewState state) {
        if(state.isSaved()) {
            showTasksList();
            return;
        }

        if(state.isEmpty()) {
            showEmptyTaskError();
        }

        if(!state.title().isEmpty()) {
            setTitle(state.title());
        }

        if(!state.description().isEmpty()) {
            setDescription(state.description());
        }
    }

    private void showEmptyTaskError() {
        Snackbar.make(title, getString(R.string.loading_tasks_error), Snackbar.LENGTH_SHORT).show();
    }

    private void showTasksList() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private void setTitle(String title) {
        this.title.setText(title);
    }

    private void setDescription(String description) {
        this.description.setText(description);
    }
}
