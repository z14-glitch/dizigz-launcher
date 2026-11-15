package com.dizigz.launcher.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dizigz.launcher.core.LaunchSettings;
import com.dizigz.launcher.core.OfflineProfile;

/** Neon Purple, горизонтальный интерфейс без тяжёлых анимаций и внешних библиотек. */
public class MainActivity extends Activity {
    private static final int BG = 0xFF0B0614, PANEL = 0xFF1A1030, NEON = 0xFFB026FF,
            TEXT = 0xFFEDE7FF, MUTED = 0xFF9A8DBF, WARN = 0xFFFFB454;

    private SharedPreferences prefs;
    private FrameLayout content;
    private long totalMemMb;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("dizigz", MODE_PRIVATE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(mi);
        totalMemMb = mi.totalMem / (1024 * 1024);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(BG);

        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setPadding(dp(12), dp(16), dp(12), dp(16));
        rail.setBackground(rounded(PANEL, 0));
        TextView logo = text("DIZIGZ", 18, NEON, true);
        logo.setPadding(dp(8), 0, dp(8), dp(16));
        rail.addView(logo);
        rail.addView(navButton("Играть", this::showPlay));
        rail.addView(navButton("Версии", () -> showSoon("Версии", "Каталог версий Minecraft, Vanilla / Fabric / Forge / NeoForge / OptiFine.")));
        rail.addView(navButton("Моды", () -> showSoon("Моды", "Встроенный каталог и установка модов.")));
        rail.addView(navButton("Скины", () -> showSoon("Скины", "Каталог и импорт PNG-скинов.")));
        rail.addView(navButton("Настройки", this::showSettings));
        root.addView(rail, new LinearLayout.LayoutParams(dp(150), ViewGroup.LayoutParams.MATCH_PARENT));

        content = new FrameLayout(this);
        content.setPadding(dp(24), dp(16), dp(24), dp(16));
        root.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        setContentView(root);
        showPlay();
    }

    private void show(View v) { content.removeAllViews(); content.addView(v); }

    private void showPlay() {
        LinearLayout col = column();
        col.addView(text("Dizigz Launcher", 28, TEXT, true));
        col.addView(text("Offline-профиль", 14, MUTED, false));

        final EditText nick = new EditText(this);
        nick.setHint("Ник (3–16: A-Z, a-z, 0-9, _)");
        nick.setHintTextColor(MUTED);
        nick.setTextColor(TEXT);
        nick.setSingleLine(true);
        nick.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        nick.setText(prefs.getString("nick", ""));
        nick.setBackground(rounded(PANEL, dp(10)));
        nick.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(320), ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        col.addView(nick, lp);

        final TextView status = text("Версия не выбрана (каталог версий — следующий этап).", 13, MUTED, false);
        status.setPadding(0, dp(12), 0, dp(12));
        col.addView(status);

        TextView play = text("ИГРАТЬ", 20, Color.WHITE, true);
        play.setGravity(Gravity.CENTER);
        play.setBackground(rounded(NEON, dp(14)));
        play.setOnClickListener(v -> {
            String n = nick.getText().toString().trim();
            if (!OfflineProfile.isValidName(n)) {
                status.setTextColor(WARN);
                status.setText("Ник: 3–16 символов, только A-Z, a-z, 0-9 и _.");
                return;
            }
            prefs.edit().putString("nick", n).apply();
            OfflineProfile p = OfflineProfile.create(n);
            status.setTextColor(WARN);
            status.setText("Профиль сохранён (UUID " + p.uuid + "). Запуск игры пока недоступен: Java-движок не подключён.");
        });
        col.addView(play, new LinearLayout.LayoutParams(dp(220), dp(56)));
        show(col);
    }

    private void showSettings() {
        final LaunchSettings s = new LaunchSettings();
        s.ramMb = prefs.getInt("ram", LaunchSettings.recommendedRamMb(totalMemMb));
        try { s.graphics = LaunchSettings.Graphics.valueOf(prefs.getString("gfx", "LOW")); }
        catch (IllegalArgumentException ignored) { s.graphics = LaunchSettings.Graphics.LOW; }

        ScrollView sv = new ScrollView(this);
        LinearLayout col = column();
        sv.addView(col);
        col.addView(text("Настройки", 28, TEXT, true));
        col.addView(text("Память устройства: " + totalMemMb + " МБ. Рекомендуется: "
                + LaunchSettings.recommendedRamMb(totalMemMb) + " МБ.", 13, MUTED, false));
        if (LaunchSettings.deviceTooWeak(totalMemMb))
            col.addView(text("Мало ОЗУ: Minecraft может не запуститься или сильно тормозить.", 13, WARN, false));

        final TextView ramLabel = text("", 16, TEXT, false);
        ramLabel.setPadding(0, dp(16), 0, dp(4));
        col.addView(ramLabel);
        SeekBar bar = new SeekBar(this);
        int steps = (LaunchSettings.MAX_RAM_MB - LaunchSettings.MIN_RAM_MB) / LaunchSettings.STEP_MB;
        bar.setMax(steps);
        bar.setProgress((LaunchSettings.clampRam(s.ramMb) - LaunchSettings.MIN_RAM_MB) / LaunchSettings.STEP_MB);
        ramLabel.setText("ОЗУ для игры: " + LaunchSettings.clampRam(s.ramMb) + " МБ");
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                int mb = LaunchSettings.MIN_RAM_MB + p * LaunchSettings.STEP_MB;
                ramLabel.setText("ОЗУ для игры: " + mb + " МБ");
                if (user) prefs.edit().putInt("ram", mb).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
        col.addView(bar, new LinearLayout.LayoutParams(dp(360), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView g = text("Графика", 16, TEXT, false);
        g.setPadding(0, dp(16), 0, dp(4));
        col.addView(g);
        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.HORIZONTAL);
        String[] names = {"Низкая", "Средняя", "Высокая"};
        LaunchSettings.Graphics[] vals = LaunchSettings.Graphics.values();
        for (int i = 0; i < vals.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(names[i]);
            rb.setTextColor(TEXT);
            rb.setId(1000 + i);
            rg.addView(rb);
            if (vals[i] == s.graphics) rb.setChecked(true);
        }
        rg.setOnCheckedChangeListener((grp, id) -> prefs.edit().putString("gfx", vals[id - 1000].name()).apply());
        col.addView(rg);
        show(sv);
    }

    private void showSoon(String title, String desc) {
        LinearLayout col = column();
        col.addView(text(title, 28, TEXT, true));
        col.addView(text(desc, 14, MUTED, false));
        col.addView(text("Не реализовано в этой сборке.", 14, WARN, false));
        show(col);
    }

    // ---- helpers ----
    private LinearLayout column() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        return c;
    }

    private TextView navButton(String label, Runnable action) {
        TextView t = text(label, 16, TEXT, false);
        t.setPadding(dp(12), dp(12), dp(12), dp(12));
        t.setOnClickListener(v -> action.run());
        return t;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
                        }
                         
