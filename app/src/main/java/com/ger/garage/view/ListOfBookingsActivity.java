package com.ger.garage.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ger.garage.Presenter.ListOfBookingsContract;
import com.ger.garage.Presenter.PresenterListOfBookings;
import com.ger.garage.R;
import com.ger.garage.model.CacheImplementation;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.ger.garage.model.QueryType.*;

public class ListOfBookingsActivity extends AppCompatActivity implements ListViewAdapter.CheckBoxCheckedListener, ListOfBookingsContract.View {


    private ListView bookingsListView;
    private ListViewAdapter adapter;
    private ArrayList<String> bookings;
    private HashMap<Integer, String> positionsChecked;
    private ArrayList<Boolean> checkBoxes;
    private PresenterListOfBookings presenter;
    private ProgressBar progressBar;
    private CacheImplementation cache;
    private int currentPosition;
    private LocalDate fDate, sDate = null;
    private AlertDialog.Builder confirmationPrintBuilder = null;

    private final String NoBookingsSelected = "No bookings were found";
    private final String statusChangedSuccessfully = "The Status was changed successfully";
    private final String alreadyStatusUpdated = "Status was already updated by another user";
    private final String endStatus = "This is a end status, the booking can't be changed to another status";
    private final String selectStatus = "Select a status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_bookings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.progressBarListOfBookings);
        progressBar.setVisibility(View.VISIBLE);
        positionsChecked = new HashMap<>();
        bookingsListView = findViewById(R.id.ListOfBookings);
        presenter = new PresenterListOfBookings(ListOfBookingsActivity.this);
        cache = new CacheImplementation(ListOfBookingsActivity.this);
        confirmationPrintBuilder = new AlertDialog.Builder(ListOfBookingsActivity.this, R.style.MyAlertDialogStyle);
        permission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getBookings();
    }

    private void getBookings() {

        switch (cache.getQuery()) {

            case date:
                fDate = cache.getDate();
                presenter.getBookings(fDate, sDate);
                break;
            case period:
                LocalDate[] period = cache.getPeriod();
                fDate = period[0];
                sDate = period[1];
                presenter.getBookings(fDate, sDate);
                break;
            case idBooking:
                // No implemented
                break;
            case emailCustomer:
                // No implemented
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + NoExist);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_of_bookings1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

       switch (item.getItemId()) {
            case R.id.logout:
                // No implemented
                break;
            case R.id.changeStatus:
               changeStatus();
               break;
           case R.id.details:
               // No implemented
               break;
           case R.id.printInvoice:
               // No implemented
               break;
           case R.id.printSchedule:
               confirmationPrintBuilder.setMessage(R.string.confirm_dialog_print_message).create().show();
               // No implemented
               break;
           case R.id.selectAll:
               // No implemented
               break;
           case R.id.selectNone:
               // No implemented
               break;
           default:
                // No implemented

        }

        return true;

    }

    private void changeStatus() {

        Set set = positionsChecked.entrySet();
        Iterator iterator = set.iterator();
        final Integer position = (Integer) ((Map.Entry)iterator.next()).getKey();
        final String booking = bookings.get(position);
        final String[] listOfStatus = presenter.getStatus(booking);


        AlertDialog.Builder builder = new AlertDialog.Builder(ListOfBookingsActivity.this);
        builder.setTitle(selectStatus);


        if (listOfStatus.length == 0) { // Return message
            Toast.makeText(this, endStatus, Toast.LENGTH_LONG ).show();
            return;
        }
        builder.setItems(listOfStatus, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                askForConfirmation(listOfStatus[which]);
            }

            private void askForConfirmation(final String newStatus) {

                AlertDialog.Builder builderConfirmation = new AlertDialog.Builder(ListOfBookingsActivity.this, R.style.MyAlertDialogStyle);
                builderConfirmation.setMessage(R.string.confirm_dialog_message)
                        .setPositiveButton(R.string.confirm_dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               progressBar.setVisibility(View.VISIBLE);
                               currentPosition = position;
                               presenter.changeStatus(booking, newStatus);
                            }

                        })
                        .setNegativeButton(R.string.confirm_dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
                        builderConfirmation.show();

            }
        }).create();
        builder.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        invalidateOptionsMenu();

        if (positionsChecked.size() > 1) {

            menu.findItem(R.id.details).setVisible(false);
            menu.findItem(R.id.changeStatus).setVisible(false);
            menu.findItem(R.id.printSchedule).setVisible(true);
            menu.findItem(R.id.printInvoice).setVisible(false);


        } else if (positionsChecked.size() == 1) {

            menu.findItem(R.id.details).setVisible(true);
            menu.findItem(R.id.changeStatus).setVisible(true);
            menu.findItem(R.id.printSchedule).setVisible(false);
            menu.findItem(R.id.printInvoice).setVisible(true);

        } else {

            menu.findItem(R.id.details).setVisible(false);
            menu.findItem(R.id.changeStatus).setVisible(false);
            menu.findItem(R.id.printInvoice).setVisible(false);
            menu.findItem(R.id.printSchedule).setVisible(false);

        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public void getCheckBoxCheckedListener(int position, Boolean isChecked) {

        if (isChecked) {
            positionsChecked.put(position, bookings.get(position));
            checkBoxes.set(position, true);
        }
        else {
            positionsChecked.remove(position);
            checkBoxes.set(position, false);
        }
    }

    @Override
    protected void onDestroy() {

        presenter.detach();
        presenter = null;
        bookings.clear();
        bookings = null;
        cache = null;
        bookingsListView.setAdapter(null);
        bookingsListView = null;
        adapter.clear();
        adapter = null;
        super.onDestroy();
    }

    @Override
    public void showBookings(ArrayList<String> bookings) {
        // Bookings screens
        this.bookings = bookings;
        // If checkboxes is null, then we should initialize this structure
        // which contain values of the checkboxes (true or false)
        // same size like bookings
        if (checkBoxes == null) {
            checkBoxes = new ArrayList<>();
            for (int i = 0; i < this.bookings.size(); i++)
                checkBoxes.add(false);
        }
        if (checkBoxes.size() != bookings.size()) {

            int difference = bookings.size() - checkBoxes.size();

            for (int i = 0; i < difference; i++)
                checkBoxes.add(false);

        }
        adapter = new ListViewAdapter(this.bookings,checkBoxes,ListOfBookingsActivity.this);
        bookingsListView.setAdapter(adapter);
        adapter.setCheckedListener(this);
        this.bookings = bookings;
        if (this.bookings.isEmpty())
            Toast.makeText(this, NoBookingsSelected, Toast.LENGTH_SHORT ).show();
        if (progressBar != null && progressBar.isShown()) {
            progressBar.setVisibility(View.GONE);
        }
        if (currentPosition != 0) {
            bookingsListView.setSelection(currentPosition);
        }

    }

    @Override
    public void showErrorMessage(String errorMessage) {

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT ).show();
        if (progressBar != null && progressBar.isShown())
            progressBar.setVisibility(View.GONE);

    }

    @Override
    public void showSuccessUpdateStatusMessage() {
        if (ListOfBookingsActivity.this != null)
            Toast.makeText(this, statusChangedSuccessfully, Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void showStatusAlreadyUpdatedMessage() {
        if (ListOfBookingsActivity.this != null)
            Toast.makeText(this, alreadyStatusUpdated, Toast.LENGTH_SHORT ).show();

    }

    @Override
    protected void onPause() {
        presenter.detach();
        super.onPause();
    }

    @Override
    protected void onStop() {
        presenter.detach();
        super.onStop();
    }


    private void permission() {

        Dexter.withActivity(ListOfBookingsActivity.this)
              .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
              .withListener(new PermissionListener() {
                  @Override
                  public void onPermissionGranted(PermissionGrantedResponse response) {

                              confirmationPrintBuilder.setPositiveButton(R.string.confirm_dialog_confirm, new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {
                                      createPDFFile(Common.getAppPath(ListOfBookingsActivity.this)+"schedule.pdf");
                                  }

                              })
                              .setNegativeButton(R.string.confirm_dialog_cancel, new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {

                                      int i =+ 1;
                                  }
                              });
                  }

                  @Override
                  public void onPermissionDenied(PermissionDeniedResponse response) {

                  }

                  @Override
                  public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                  }
              })
                .check();


    }

    private void createPDFFile(String path) {

        if (new File(path).exists())
                new File(path).delete();

        try {
            Document document = new Document();
            // Save
            PdfWriter.getInstance(document, new FileOutputStream(path));
            // Open to write document
            document.open();

            // Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();

            // Add creator
            document.addCreator("admin@gmail.com");

            // Font setting
            BaseColor colorAccent = new BaseColor(0, 153, 204, 255);
            float fontSize = 20.0f;
            float valueFontSize = 26.0f;

            // Custom font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);

            // Create title of document

            Font titleFont = new Font(fontName, 36.0f, Font.NORMAL, BaseColor.BLACK);

            addText(document, "Bookings Details", Element.ALIGN_CENTER, titleFont);

            // Add more
            Font reference = new Font(fontName, valueFontSize, Font.NORMAL, BaseColor.BLACK);
            if ((fDate != null) && (sDate != null)) {
                addText(document, "From: " + fDate.toString() + " to: " + sDate.toString(), Element.ALIGN_LEFT, reference);
            } else if ((fDate != null) && (sDate == null)) {
                addText(document, "Date: " + fDate.toString(), Element.ALIGN_LEFT, reference);
            } else {
                addText(document, "Customize", Element.ALIGN_LEFT, reference);
            }

            // Loop each book

            // Add booking Loop

            String bookingText;

            for (Map.Entry<Integer, String> b: positionsChecked.entrySet()) {

                addLineSeparator(document);

                bookingText = b.getValue();

                Font booking = new Font(fontName, fontSize, Font.NORMAL, colorAccent);
                addText(document, bookingText, Element.ALIGN_LEFT, booking);

            }

            document.close();

            Toast.makeText(ListOfBookingsActivity.this, "Success", Toast.LENGTH_SHORT).show();

            printPDF();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printPDF() {

        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        try {

            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(ListOfBookingsActivity.this, Common.getAppPath(ListOfBookingsActivity.this) + "schedule.pdf");
            printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());

        } catch (Exception ex) {
        }

    }

    private void addLineSeparator(Document document) throws DocumentException {

        LineSeparator lineSeparator = new LineSeparator();

        lineSeparator.setLineColor(new BaseColor(0,0,0,68));
        addLineSpace(document);
        document.add(new Chunk(lineSeparator));
        addLineSpace(document);

    }

    private void addLineSpace(Document document) throws DocumentException {

        document.add(new Paragraph(""));

    }

    private void addText(Document document, String text, int align, Font font) throws DocumentException {

        Chunk chunk = new Chunk(text, font);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setAlignment(align);
        document.add(paragraph);
    }



}
