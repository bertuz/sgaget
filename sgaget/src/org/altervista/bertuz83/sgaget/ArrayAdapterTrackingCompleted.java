package org.altervista.bertuz83.sgaget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.altervista.bertuz83.sgaget.business.TrackRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * User: bertuz
 * Project: sgaget
 */
public class ArrayAdapterTrackingCompleted extends ArrayAdapter<TrackRecord>{
    private IActHomeFragTabCompletareComm parentList;
    private ArrayList<TrackRecord> trackRecords;
    private boolean newOnTop= false;

    private ArrayList<Long> trackRecordsNotToAdd= new ArrayList<Long>();

    public ArrayAdapterTrackingCompleted(Context context, ArrayList<TrackRecord> trackRecordsToComplete, IActHomeFragTabCompletareComm parentList) {
        super(context, R.id.tab_completare_txtview, trackRecordsToComplete);
        this.parentList = parentList;
        this.trackRecords= trackRecordsToComplete;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final long idSelected= this.trackRecords.get(position).getCreationDate();
        final int positionSelected= position;

        LayoutInflater li= LayoutInflater.from(getContext());
        convertView= li.inflate(R.layout.item_track_to_complete, parent, false);

        ((TextView)convertView.findViewById(R.id.item_track_to_complete_from)).setText("DA: " + getItem(position).getStartCandidates().get(0).getHotpointName());
        ((TextView)convertView.findViewById(R.id.item_track_to_complete_to)).setText("A: " + getItem(position).getFinishCandidates().get(0).getHotpointName());
        ((TextView)convertView.findViewById(R.id.item_track_to_complete_elapsedTime)).setText(getItem(position).getElapsedTime());

        Date elapsedTime= new Date(idSelected);
        SimpleDateFormat df= new SimpleDateFormat("dd/MM/yy");
        ((TextView)convertView.findViewById(R.id.item_track_to_complete_date)).setText(df.format(elapsedTime));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentList.onTrackrecordClicked(idSelected);
            }
        });

        convertView.findViewById(R.id.item_track_to_complete_btn_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
                builder.setTitle("Elimina tragitto?");
                builder.setCancelable(true);
                builder.setMessage("Sei sicur* di eliminare questo tragitto?");
                builder.setPositiveButton("Sì", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        ArrayAdapterTrackingCompleted.this.trackRecords.remove(getItem(positionSelected));
                        notifyDataSetChanged();
                        dialog.dismiss();
                        parentList.onDeleteTrackrecord(idSelected);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        return convertView;
    }


    public void removeItem(long creationDate){
        //todo implementare tramite looper
        for(TrackRecord trackRecord : this.trackRecords){
            if(trackRecord.getCreationDate() == creationDate){
                remove(trackRecord);
                this.trackRecords.remove(trackRecord);
                return;
            }
        }
        //ricezione del messaggio di rimozione prima di quello di aggiunta
        trackRecordsNotToAdd.add(creationDate);
    }


    public void removeAllItems(){
        this.trackRecords.clear();
        clear();
        notifyDataSetChanged();
    }


    public void addItem(TrackRecord trackRecord){
        /*
            caso in cui un trackrecord e' stato creato (arriva un messaggio di aggiunta alla lisa)
            ma poi completato immediatamente (arriva un messaggio di rimozione).
            Il messaggio di rimozione potrebbe arrivare prima di quello di aggiunta, aggiungendolo cosi erroneamente.
            (i messaggi sono gestiti da un thread separato)
            SOLUZIONE PIU ELEGANTE: tramite looper e handler, ma con opportune modifiche non banali.
        */
        for(Long trackRecordCheck : trackRecordsNotToAdd){
            if(trackRecordCheck.compareTo(trackRecord.getCreationDate())==0){
                trackRecordsNotToAdd.remove(trackRecordCheck);
                return;
            }
        }
        if(newOnTop)
            insert(trackRecord, 0);
        else
            add(trackRecord);

        notifyDataSetChanged();
    }


    public void changeOrder(boolean newOnTop){
        if(this.newOnTop != newOnTop){
            this.newOnTop = newOnTop;

            Collections.reverse(this.trackRecords);
            this.notifyDataSetChanged();
        }
    }
}
