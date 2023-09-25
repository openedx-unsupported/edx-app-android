package org.edx.mobile.whatsnew;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.databinding.FragmentWhatsNewItemBinding;
import org.edx.mobile.model.whatsnew.WhatsNewItemModel;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.images.ImageUtils;
import org.edx.mobile.util.images.ImageUtils.MimeType;

public class WhatsNewItemFragment extends BaseFragment {
    public static final String ARG_MODEL = "ARG_MODEL";

    private FragmentWhatsNewItemBinding binding;

    public static WhatsNewItemFragment newInstance(@NonNull WhatsNewItemModel model) {
        final WhatsNewItemFragment fragment = new WhatsNewItemFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_MODEL, model);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWhatsNewItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            final WhatsNewItemModel model = args.getParcelable(ARG_MODEL);

            binding.title.setText(escapePlatformName(model.getTitle()));
            binding.message.setText(escapePlatformName(model.getMessage()));
            binding.message.setMovementMethod(new ScrollingMovementMethod());

            @DrawableRes final int imageRes = UiUtils.INSTANCE.getDrawable(requireContext(), model.getImage());
            MimeType mimeType = ImageUtils.getDrawableMimeType(requireContext(), imageRes);
            // Place-holder is necessary otherwise glide will not load the gif properly.
            if (mimeType == MimeType.GIF) {
                Glide.with(binding.image.getContext()).asGif()
                        .load(imageRes)
                        .placeholder(R.drawable.login_screen_image)
                        .into(binding.image);
            } else {
                binding.image.setImageResource(imageRes);
            }
        }
    }

    private String escapePlatformName(@NonNull String input) {
        return input.replaceAll("platform_name", getString(R.string.platform_name));
    }
}
