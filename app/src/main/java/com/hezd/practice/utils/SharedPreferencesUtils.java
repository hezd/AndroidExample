package com.hezd.practice.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import timber.log.Timber;

/**
 * @author hezd
 * @date 2023/9/26 11:15
 * @description
 */
public class SharedPreferencesUtils {

    private static final String TAG = "SharedPreferencesUtils";
    Context context;
    String name;

    public SharedPreferencesUtils(Context context, String name) {
        this.context = context;
        this.name = name;
    }

    /**
     * 根据key和预期的value类型获取value的值
     *
     * @param key
     * @param clazz
     * @return
     */
    public <T> T getValue(String key, Class<T> clazz) {
        if (context == null) {
            throw new RuntimeException("请先调用带有context，name参数的构造！");
        }
        SharedPreferences sp = this.context.getSharedPreferences(this.name,
                Context.MODE_PRIVATE);
        return getValue(key, clazz, sp);
    }

    /**
     * 针对复杂类型存储<对象>
     *
     * @param key
     */
    public void setObject(String key, Object object) {
        SharedPreferences sp = this.context.getSharedPreferences(this.name, Context.MODE_PRIVATE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(object);
            String objectVal = new String(Base64.encode(baos.toByteArray(),
                    Base64.DEFAULT));
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, objectVal);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 保存对象
    }

    public void removeInfo(SharedPreferences sharedPreferences, String key) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> clazz) {
        SharedPreferences sp = this.context.getSharedPreferences(this.name,
                Context.MODE_PRIVATE);
        if (sp.contains(key)) {
            String objectVal = sp.getString(key, null);
            if (objectVal != null) {
                byte[] buffer = Base64.decode(objectVal, Base64.DEFAULT);
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(bais);
                    T t = (T) ois.readObject();
                    return t;
                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bais != null) {
                            bais.close();
                        }
                        if (ois != null) {
                            ois.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 对于外部不可见的过渡方法
     *
     * @param key
     * @param clazz
     * @param sp
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T getValue(String key, Class<T> clazz, SharedPreferences sp) {
        T t;
        try {
            t = clazz.newInstance();
            if (t instanceof Integer) {
                return (T) Integer.valueOf(sp.getInt(key, 0));
            } else if (t instanceof String) {
                return (T) sp.getString(key, "");
            } else if (t instanceof Boolean) {
                return (T) Boolean.valueOf(sp.getBoolean(key, false));
            } else if (t instanceof Long) {
                return (T) Long.valueOf(sp.getLong(key, 0L));
            } else if (t instanceof Float) {
                return (T) Float.valueOf(sp.getFloat(key, 0L));
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
            Timber.tag(TAG).d("类型输入错误或者复杂类型无法解析[" + e.getMessage() + "]");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Timber.tag(TAG).d( "类型输入错误或者复杂类型无法解析[" + e.getMessage() + "]");
        }
        Log.e("system", "无法找到" + key + "对应的值");
        return null;
    }


    private static SharedPreferences preferences;

    public static void getPreference(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        }
    }

    /**
     * @param context 上下文 最好用Application的context
     * @param key     属性名
     * @param value   属性值
     */
    public static void putBoolean(Context context, String key, boolean value) {
        getPreference(context);
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * @param context      上下文 最好用Application的context
     * @param key          属性名
     * @param defaultValue 属性值 默认
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        getPreference(context);
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * @param context 上下文 最好用Application的context
     * @param key     属性名
     * @param value   属性值
     */
    public static void putFloat(Context context, String key, float value) {
        getPreference(context);
        preferences.edit().putFloat(key, value).commit();
    }

    /**
     * @param context      上下文 最好用Application的context
     * @param key          属性名
     * @param defaultValue 属性值 默认
     */
    public static float getFloat(Context context, String key, float defaultValue) {
        getPreference(context);
        return preferences.getFloat(key, defaultValue);
    }

    /**
     * @param context 上下文 最好用Application的context
     * @param key     属性名
     * @param value   属性值
     */
    public static void putString(Context context, String key, String value) {
        getPreference(context);
        preferences.edit().putString(key, value).apply();
    }

    /**
     * @param context      上下文 最好用Application的context
     * @param key          属性名
     * @param defaultValue 属性值 默认
     */
    public static String getString(Context context, String key, String defaultValue) {
        getPreference(context);
        return preferences.getString(key, defaultValue);
    }

    /**
     * @param context 上下文 最好用Application的context
     * @param key     属性名
     * @param value   属性值
     */
    public static void putLong(Context context, String key, long value) {
        getPreference(context);
        preferences.edit().putLong(key, value).commit();
    }

    /**
     * @param context      上下文 最好用Application的context
     * @param key          属性名
     * @param defaultValue 属性值 默认
     */
    public static long getLong(Context context, String key, long defaultValue) {
        getPreference(context);
        return preferences.getLong(key, defaultValue);
    }

    /**
     * @param context 上下文 最好用Application的context
     * @param key     属性名
     * @param value   属性值
     */
    public static void putInt(Context context, String key, int value) {
        getPreference(context);
        preferences.edit().putInt(key, value).commit();
    }

    /**
     * @param context      上下文 最好用Application的context
     * @param key          属性名
     * @param defaultValue 属性值 默认
     */
    public static int getInt(Context context, String key, int defaultValue) {
        getPreference(context);
        return preferences.getInt(key, defaultValue);
    }


}
