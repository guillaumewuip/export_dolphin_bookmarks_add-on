package com.dolphin.browser.extension.exportdolphinbookmarks;


import com.dolphin.browser.addons.AddonService;
import com.dolphin.browser.addons.AlertDialogBuilder;
import com.dolphin.browser.addons.BookmarkTreeNode;
import com.dolphin.browser.addons.Browser;
import com.dolphin.browser.addons.OnClickListener;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;



public class MyAddonService extends AddonService {

    private Browser B; //Dolphin
    private File path; //Path to save the file
    private int tab = 1; //Save the current indentation

    /**
     * onBrowserConnected
     *
     * When Dolphin connect to the app
     *
     * @param browser
     */
    @Override
    protected void onBrowserConnected(Browser browser) {

        try {

            //Save the browser
            B = browser;

            //Show add-on bar action
            browser.addonBarAction.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
            browser.addonBarAction.setTitle("Export Bookmarks");

            //Set a listener on click
            browser.addonBarAction.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(Browser browser) {

                    showDialog(browser);

                }
            });

            browser.addonBarAction.show();

        } catch (Exception e) {
            Log.d("onBrowserConnected","Can't show addon Bar");
        }
    }


    /**
     * showDialog
     *
     * Open a dialog asking the user to export his bookmarks in sd
     *
     * @param browser
     */
    private void showDialog(Browser browser) {

        //Save the path to the Download folder
        //TODO : ask the user for path ?
        path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
        );

        //Show dialog with path url
        AlertDialogBuilder builder = new AlertDialogBuilder();
        builder.setTitle("Export");
        builder.setMessage("Export Bookmarks in " + path + " ?");

        //Set a listener on click
        builder.setPositiveButton("Ok", AddonService.getInstance().obtainOnClickListenerMessage(exportListener));

        try {
            browser.window.showDialog(builder);
        } catch (Exception e) {
            Log.d("showDialog","Can't show addon dialog");
            //e.printStackTrace();
        }
    }

    /**
     * exportListener
     *
     * Fired by the user pressing "Ok" button
     * It will generate a html file following the bookmarks export convention
     * @see http://msdn.microsoft.com/en-us/library/aa753582(v=vs.85).aspx
     *
     */
    private OnClickListener exportListener = new OnClickListener() {

        @Override
        public void onClick(Browser browser) {

            try{

                Log.d("exportListener", "Ok");

                //Create the file bookmarks.html as OutputStream
                //TODO : check if path is reachable
                File file = new File(path, "bookmarks.html");
                FileOutputStream fos = new FileOutputStream(file);

                //Write head of html
                fos.write(("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                        "    <!--This is an automatically generated file.\n" +
                        "    It will be read and overwritten.\n" +
                        "    Do Not Edit! -->\n" +
                        "    <META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                        "    <Title>Bookmarks</Title>\n" +
                        "    <H1>Bookmarks</H1>\n").getBytes());

                fos.write((makeTab(tab) + "<DL><p>\n").getBytes());

                tab++;

                fos.flush();

                //Get all the dolphin bookmarks in root
                List<BookmarkTreeNode> nodes = B.bookmarks.searchBookmarkTreeNodes(
                    BookmarkTreeNode.KEY_PARENT + " = 0", //Root
                    null
                );

                dispatch(nodes, fos);

                tab--;

                fos.write((makeTab(tab) +"</DL><p>\n").getBytes());
                fos.flush();

                fos.close();

                //The export is completed
                Log.d("exportListener", "Finish !");
                Toast.makeText(MyAddonService.this, "Bookmarks saved !", Toast.LENGTH_SHORT).show();

            }
            catch (IOException e) {
                Log.d("exportListener", "IOException");
                //e.printStackTrace();
            }
            catch (RemoteException e) {
                Log.d("exportListener", "RemoteException");
                //e.printStackTrace();
            }

        }
    };


    /**
     * dispatch
     *
     * Dispatch nodes in two function according they are folder or not
     * Pass the fileOutputStream
     *
     * @param nodes
     * @param fos
     */
    private void dispatch(List<BookmarkTreeNode> nodes, FileOutputStream fos) {

        for(BookmarkTreeNode node : nodes) {

            boolean isFolder = node.isFolder();

            if(isFolder){

                saveFolder(node, fos);

            } else {

                saveItem(node, fos);

            }

            //Log.d("node", node.title);
        }
    }

    /**
     * saveFolder
     *
     * We received a folder in param
     * Let's construct the tree for him
     *
     * @param folder
     * @param fos
     */
    private void saveFolder(BookmarkTreeNode folder, FileOutputStream fos){

        try{

            String title = folder.title;
            long id      = folder.id;

            fos.write((makeTab(tab) + "<DT>").getBytes());

                fos.write(("<H3>" + title + "</H3>\n").getBytes());
                fos.write((makeTab(tab) + "<DL><p>\n").getBytes());

                tab++;

                List<BookmarkTreeNode> nodes = B.bookmarks.searchBookmarkTreeNodes(
                        BookmarkTreeNode.KEY_PARENT + " =?",
                        new String[] {
                                String.valueOf(id)
                        }
                );

                //Dispatch folder's children
                dispatch(nodes, fos);

                tab--;

                fos.write((makeTab(tab) +"</DL><p>\n").getBytes());

            fos.flush();
        }
        catch(Exception e) {
            Log.d("saveFolder", "error");
        }


    }

    /**
     * saveItem
     *
     * We received a item in param
     * We save him in the html
     *
     * @param item
     * @param fos
     */
    private void saveItem(BookmarkTreeNode item, FileOutputStream fos){

        try{

            String title = item.title;
            String url   = item.url;

            fos.write((makeTab(tab) +"<DT>").getBytes());

                fos.write(("<A HREF=\"" + url + "\">" + title + "</A>\n").getBytes());

            fos.flush();

        }
        catch (Exception e) {
            Log.d("saveItem", "error");
        }
    }

    /**
     * makeTab
     *
     * An helper to indent all this stuff
     *
     * @param nb
     * @return
     */
    private String makeTab(int nb) {

        String t = "";

        for(int i = 0; i<nb; i++) {
            t += "    ";
        }

        return t;
    }


    @Override
    protected void onBrowserDisconnected(Browser browser) {}
}
