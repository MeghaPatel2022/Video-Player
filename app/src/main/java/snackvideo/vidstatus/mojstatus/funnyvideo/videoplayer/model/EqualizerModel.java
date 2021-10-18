package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model;

public class EqualizerModel {
    boolean isEnable;
    int preset;
    int vs1, vs2, vs3, vs4, vs5;
    int reverb;
    int bass, virtualizer;

    public int getReverb() {
        return reverb;
    }

    public void setReverb(int reverb) {
        this.reverb = reverb;
    }

    public int getPreset() {
        return preset;
    }

    public void setPreset(int preset) {
        this.preset = preset;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public int getVs1() {
        return vs1;
    }

    public void setVs1(int vs1) {
        this.vs1 = vs1;
    }

    public int getVs2() {
        return vs2;
    }

    public void setVs2(int vs2) {
        this.vs2 = vs2;
    }

    public int getVs3() {
        return vs3;
    }

    public void setVs3(int vs3) {
        this.vs3 = vs3;
    }

    public int getVs4() {
        return vs4;
    }

    public void setVs4(int vs4) {
        this.vs4 = vs4;
    }

    public int getVs5() {
        return vs5;
    }

    public void setVs5(int vs5) {
        this.vs5 = vs5;
    }


    public int getBass() {
        return bass;
    }

    public void setBass(int bass) {
        this.bass = bass;
    }

    public int getVirtualizer() {
        return virtualizer;
    }

    public void setVirtualizer(int virtualizer) {
        this.virtualizer = virtualizer;
    }
}
