package exlock.phonecode_pc.EditFeatures.Block;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import exlock.phonecode_pc.EditFeatures.CustomDialog.CategoryDialogActivity;
import exlock.phonecode_pc.EditFeatures.ItemTouchHelperAdapter;
import exlock.phonecode_pc.R;
import exlock.phonecode_pc.Tools.ManageCode;

enum MenuList {
    REMOVE, EDIT, ADD_BELOW, GROUP_EDIT
}

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ManageCode mc;
    private ArrayList<TextView> lineNumber = new ArrayList<>();


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        this.mc.updateLine();
        ArrayList<String> lines = this.mc.getLines();
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(lines, i, i + 1);
        else
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(lines, i, i - 1);

        TextView fromLineNumber = this.lineNumber.get(fromPosition);
        TextView toLineNumber = this.lineNumber.get(toPosition);
        CharSequence fromLN = fromLineNumber.getText();
        CharSequence toLN = toLineNumber.getText();
        fromLineNumber.setText(toLN);
        toLineNumber.setText(fromLN);

        this.lineNumber.set(toPosition, fromLineNumber);
        this.lineNumber.set(fromPosition, toLineNumber);

        this.mc.setListAsContent(lines);
        this.mc.updateLine();

        this.notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    public BlockAdapter(ManageCode mc, OnStartDragListener dragStartListener){
        this.mc = mc;
        this.mDragStartListener = dragStartListener;
    }
    public List<BlockLists> blocks = new ArrayList<>();
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView func1;
        private final TextView func2;
        private final EditText arg;
        private final LinearLayout block;

        private final TextView lineNumber;
        ViewHolder(final View v){
            super(v);
            this.arg = v.findViewById(R.id.argEditText);
            this.lineNumber = v.findViewById(R.id.line_number);
            this.func1 = v.findViewById(R.id.func1);
            this.func2 = v.findViewById(R.id.func2);
            this.block = v.findViewById(R.id.block);
        }
        TextView getFunc1() {
            return this.func1;
        }
        TextView getFunc2() {
            return this.func2;
        }
        EditText getArg() {
            return this.arg;
        }
        TextView getLineNumber(){
            return this.lineNumber;
        }

        LinearLayout getblock() {
            return this.block;
        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_block, viewGroup, false);
        return new ViewHolder(v);
    }
    private final OnStartDragListener mDragStartListener;


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        position = holder.getAdapterPosition();

        final int pos = position;

        String funcString1 = this.blocks.get(position).func1;
        String arg = this.blocks.get(position).arg;
        String funcString2 = this.blocks.get(position).func2;

        holder.getblock().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createDialog(holder, pos);
                return true;
            }
        });

        this.lineNumber.add(holder.getLineNumber());
        boolean isFunc1Empty = funcString1.equals("");
        holder.getFunc1().setVisibility(isFunc1Empty ? View.INVISIBLE : View.VISIBLE);
        holder.getArg().setVisibility(funcString2.equals("") ? View.INVISIBLE : View.VISIBLE);
        holder.getFunc2().setVisibility(isFunc1Empty ? View.INVISIBLE : View.VISIBLE);
        holder.getArg().setText(arg);
        holder.getArg().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mc.updateLine();
                mc.setLine(pos, funcString1+s+funcString2);
                mc.updateLine();
            }
        });
        holder.getFunc2().setText(funcString2);
        holder.getLineNumber().setText(" "+(position+1)+" ");
        holder.getLineNumber().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
        if(isFunc1Empty) {
            return;
        }
        holder.getFunc1().setText(funcString1);
        holder.getFunc2().setVisibility(View.VISIBLE);
    }

    private void createDialog(@NotNull ViewHolder holder, final int position){

        final Context context = holder.getblock().getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] defaultMenu = context.getResources().getStringArray(R.array.menu_block_array);
        ArrayList<String> items = new ArrayList<>();
        for(int i = 0;i<defaultMenu.length;i++){
            items.add(defaultMenu[i]);
        }

        builder.setTitle("")
                .setItems(items.toArray(new CharSequence[items.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int which) {
                        MenuList[] values = MenuList.values();
                        switch (values[which]){
                            case REMOVE:
                                mc.updateLine();
                                mc.removeLine(position);
                                mc.getBlockAdapter().notifyItemRemoved(position);
                                mc.updateLine();
                                mc.notifyUpdatesInUI();
                                mc.updateUI();
                                break;
                            case EDIT:
                                editBlockDialog(context, position).show();
                                break;
                            case ADD_BELOW:
                                CategoryDialogActivity cda = new CategoryDialogActivity(context);
                                cda.init(mc, position);
                                cda.show();
                                break;
                        }
                    }
                });
        builder.show();
    }
    @Override
    public int getItemCount() {
        return blocks.size();
    }
    private Dialog editBlockDialog(final Context context, final int position){
        final EditText et = new EditText(context);
        et.setLines(1);
        et.setSingleLine();
        this.mc.updateLine();
        et.setText(this.mc.getLine(position));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Edit line "+(position+1))
                .setView(et)
                .setPositiveButton("change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        InputMethodManager mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(mInputMethodManager != null)
                            mInputMethodManager.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        mc.updateLine();
                        mc.setLine(position, et.getText().toString());
                        mc.getBlockAdapter().notifyItemChanged(position);
                        mc.updateUI();
                    }
                })
                .setNegativeButton("cancel", null)
                .create();
        return dialog;
    }
}
class Ascending implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        return o1.compareTo(o2);
    }
}