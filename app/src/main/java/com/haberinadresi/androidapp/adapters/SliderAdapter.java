package com.haberinadresi.androidapp.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haberinadresi.androidapp.R;

public class SliderAdapter extends PagerAdapter {

    private Context context;

    private static final String SLIDE_DESC_1 = "Sol üstteki açılır menü ikonundan kaynaklarınızı düzenleyebilir, " +
            "kayıtlı haber/köşe yazılarınıza ve diğer ayarlara ulaşabilirsiniz.";
    private static final String SLIDE_DESC_2 = "Sergilenen haberler arasında kelime bazlı arama yapabilir, " +
            "3 farklı haber gösterim şeklinden dilediğinizi seçebilirsiniz.";
    private static final String SLIDE_DESC_3 = "Haberi, favorilerinize ekleyebilir / arkadaşlarınızla paylaşabilirsiniz";
    private static final String SLIDE_DESC_4 = "Haber kaynağı kutucuğunun herhangi bir yerine tıklayarak ekleme veya çıkarma " +
            "işlemini gerçekleştirebilirsiniz.";
    private static final String SLIDE_DESC_5 = "Sağ üstteki arama ikonuna basarak istediğiniz yazar/kaynağa daha hızlı " +
            "ulaşabilirsiniz.";
    private static final String SLIDE_DESC_6 = "Kategorilerin yerini değiştirerek, istemediğiniz kategoriyi silerek, " +
            "kişisel kategori düzeninizi oluşturabilirsiniz.";
    private static final String SLIDE_DESC_7 = "İstediğiniz kaynağa tıklayarak sadece o haber kaynağına ait haberleri görebilirsiniz.";
    private static final String SLIDE_DESC_8 = "Ana menüden Ayarlar'a tıklayarak Gece modu, Mobil Veri Tasarrufu, " +
            "Haber Bildirimleri vb. ayarlara ulaşabilirsiniz.";
    private static final String SLIDE_DESC_9 = "Uygulama içerisinde karşılaştığınız sorunlardan dolayı olumsuz yorum yazmak yerine " +
            "sorunları bu linkten bize bildirebilirsiniz.";


    public SliderAdapter(Context context){
        this.context = context;
    }

    private int[] slide_images = {R.drawable.slide_1, R.drawable.slide_2, R.drawable.slide_3,
            R.drawable.slide_4, R.drawable.slide_5, R.drawable.slide_6, R.drawable.slide_7, R.drawable.slide_8, R.drawable.slide_9};

    private String[] slide_descriptions = {SLIDE_DESC_1, SLIDE_DESC_2, SLIDE_DESC_3,
            SLIDE_DESC_4, SLIDE_DESC_5, SLIDE_DESC_6, SLIDE_DESC_7, SLIDE_DESC_8, SLIDE_DESC_9};

    @Override
    public int getCount() {
        return slide_descriptions.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_slide, container, false);

        ImageView slideImage = view.findViewById(R.id.slide_image);
        TextView slideText = view.findViewById(R.id.slide_text);

        slideImage.setImageResource(slide_images[position]);
        slideText.setText(slide_descriptions[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((RelativeLayout) object);
    }
}
