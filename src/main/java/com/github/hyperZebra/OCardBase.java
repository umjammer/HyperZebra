package com.github.hyperZebra;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JScrollPane;


public class OCardBase extends OObject {

    OStack stack;
    BufferedImage pict;
    Boolean showPict = true;
    MyLabel label;
    OPicture picture;
    String bitmapName;

    boolean changed;

    // ボタン、フィールド情報
    ArrayList<OObject> partsList;
    ArrayList<OButton> btnList;
    ArrayList<OField> fldList;
    ArrayList<JScrollPane> paneList = new ArrayList<>();

    @Override
    public void clean() {
        // stack.mainPane.removeAll(); // すでに新しいスタックを読み込んでいるのでremoveしてはいけない
        stack = null;
        pict = null;
        label = null;
        picture = null;
        bitmapName = null;
        partsList = null;
        btnList = null;
        fldList = null;
        paneList = null;
    }

    public OCardBase(OStack st) {
        stack = st;
    }

    static void buidParts(OCardBase cdbase) {
        for (int i = cdbase.partsList.size() - 1; i >= 0; i--) {
            OObject obj = cdbase.partsList.get(i);
            if (obj.getClass() == OButton.class) {
                OButton.buildOButton((OButton) obj);
            } else {
                OField.buildOField((OField) obj);
            }
        }
    }

    public OButton GetBtnbyId(int btnId) {
        for (OButton oButton : btnList) {
            if (btnId == oButton.id)
                return oButton;
        }
        return null;
    }

    public OButton GetBtnbyNum(int number) {
        if (number - 1 >= 0 && number - 1 < btnList.size()) {
            return btnList.get(number - 1);
        }
        return null;
    }

    public OButton GetBtn(String name) {
        if (name.equals("Continue Saved Game")) {
            System.out.println("Continue Saved Game");
        }
        for (OButton oButton : btnList) {
            if (0 == oButton.name.compareToIgnoreCase(name))
                return oButton;
        }
        return null;
    }

    public OField GetFldbyId(int fldId) {
        for (OField oField : fldList) {
            if (fldId == oField.id)
                return oField;
        }
        return null;
    }

    public OField GetFldbyNum(int number) {
        if (number - 1 >= 0 && number - 1 < fldList.size()) {
            return fldList.get(number - 1);
        }
        return null;
    }

    public OField GetFld(String name) {
        for (OField oField : fldList) {
            if (0 == oField.name.compareToIgnoreCase(name))
                return oField;
        }
        return null;
    }

    public OObject GetPartbyId(int pid) {
        for (OObject oObject : partsList) {
            if (pid == oObject.id)
                return oObject;
        }
        return null;
    }


    OBackground getBg() {
        OBackground bg = null;
        if (this.objectType.equals("background")) bg = (OBackground) this;
        else if (this.objectType.equals("card")) {
            OCard cd = (OCard) this;
            if (cd.bg != null) {
                bg = cd.bg;
            } else {
                bg = cd.stack.GetBackgroundbyId(cd.bgid);
            }
        }
        return bg;
    }

    public OButton GetBgBtnbyId(int btnId) {
        OBackground bg = getBg();
        for (int i = 0; i < bg.btnList.size(); i++) {
            if (btnId == bg.btnList.get(i).id)
                return bg.btnList.get(i);
        }
        return null;
    }

    public OButton GetBgBtnbyNum(int number) {
        OBackground bg = getBg();
        if (number - 1 >= 0 && number - 1 < bg.btnList.size()) {
            return bg.btnList.get(number - 1);
        }
        return null;
    }

    public OButton GetBgBtn(String name) {
        OBackground bg = getBg();
        for (int i = 0; i < bg.btnList.size(); i++) {
            if (0 == bg.btnList.get(i).name.compareToIgnoreCase(name))
                return bg.btnList.get(i);
        }
        return null;
    }

    public OField GetBgFldbyId(int fldId) {
        OBackground bg = getBg();
        for (int i = 0; i < bg.fldList.size(); i++) {
            if (fldId == bg.fldList.get(i).id)
                return bg.fldList.get(i);
        }
        return null;
    }

    public OField GetBgFldbyNum(int number) {
        OBackground bg = getBg();
        if (number - 1 >= 0 && number - 1 < bg.fldList.size()) {
            return bg.fldList.get(number - 1);
        }
        return null;
    }

    public OField GetBgFld(String name) {
        OBackground bg = getBg();
        for (int i = 0; i < bg.fldList.size(); i++) {
            if (0 == bg.fldList.get(i).name.compareToIgnoreCase(name))
                return bg.fldList.get(i);
        }
        return null;
    }

    public int GetNumberof(OButton obj) {
        for (int number = 0; number < btnList.size(); number++) {
            if (btnList.get(number).id == obj.id) {
                return number + 1;
            }
        }
        return 0;
    }

    public int GetNumberof(OField obj) {
        for (int number = 0; number < fldList.size(); number++) {
            if (fldList.get(number).id == obj.id) {
                return number + 1;
            }
        }
        return 0;
    }

    public int GetNumberofParts(OObject obj) {
        for (int number = 0; number < partsList.size(); number++) {
            if (partsList.get(number).id == obj.id &&
                    partsList.get(number).objectType.equals(obj.objectType)) {
                return number + 1;
            }
        }
        return 0;
    }

    public void removeData() {
        PCARDFrame.pc.mainPane.removeAll();
        for (OButton oButton : btnList) {
            if (oButton.btn != null) {
                // PCARD.pc.mainPane.remove(btnList.get(i).btn);
                oButton.btn.btnData = null;
                oButton.btn = null;
            }
            if (oButton.chkbox != null) {
                // PCARD.pc.mainPane.remove(btnList.get(i).chkbox);
                oButton.chkbox.btnData = null;
                oButton.chkbox = null;
            }
            if (oButton.radio != null) {
                // PCARD.pc.mainPane.remove(btnList.get(i).radio);
                oButton.radio.btnData = null;
                oButton.radio = null;
            }
            if (oButton.popup != null) {
                // PCARD.pc.mainPane.remove(btnList.get(i).popup);
                oButton.popup.btnData = null;
                oButton.popup = null;
            }
            // btnList.get(i).card = null;
            // btnList.get(i).bkgd = null;
        }
        for (OField oField : fldList) {
            if (oField.fld != null) {
                // PCARD.pc.mainPane.remove(fldList.get(i).fld);
                oField.fld.fldData = null;
                oField.fld = null;
            }
            // fldList.get(i).card = null;
            // fldList.get(i).bkgd = null;
        }
        for (int i = 0; i < paneList.size(); i++) {
            // PCARD.pc.mainPane.remove(paneList.get(i));
        }
        for (int i = 0; i < paneList.size(); i++) {
            paneList.remove(i);
        }
        if (label != null) {
            // PCARD.pc.mainPane.remove(label);
            label.cd = null;
            label = null;
            pict = null;
            picture = null;
        }
        Arrays.fill(OButton.btnGroup, null);
    }
}
