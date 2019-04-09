package org.edx.mobile.tta.utils;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.enums.BadgeType;

public class BadgeHelper {

    public static int getBadgeIcon(BadgeType type){
        switch (type){
            case star_teacher:
                return R.drawable.t_badge_star_teacher;
            case fan:
                return R.drawable.t_badge_fan;
            case master:
                return R.drawable.t_badge_master;
            case opinion:
                return R.drawable.t_bagde_opinion;
            case evaluator:
                return R.drawable.t_badge_evaluator;
            case certificate:
                return R.drawable.t_badge_certificate;
            case inquisitive:
                return R.drawable.t_badge_inquisitive;
            case aware_listener:
                return R.drawable.t_badge_aware_listener;
            default:
                return R.drawable.t_badge_certificate;
        }
    }

}
