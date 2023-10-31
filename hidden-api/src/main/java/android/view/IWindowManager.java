package android.view;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

public interface IWindowManager extends IInterface {
    void addWindowToken(IBinder token, int type, int displayId, Bundle options);

    abstract class Stub extends Binder implements IWindowManager {
        @Override
        public IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }

        public static IWindowManager asInterface(IBinder binder) {
            throw new RuntimeException("Stub!");
        }
    }
}
