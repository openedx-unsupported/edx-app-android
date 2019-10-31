package org.humana.mobile.comparator;

import org.humana.mobile.social.SocialMember;

import java.text.Collator;
import java.util.Comparator;

public class SocialMemberComparator implements Comparator<SocialMember> {

    Collator collator;

    public SocialMemberComparator(){

        collator = Collator.getInstance();

    }

    @Override
    public int compare(SocialMember lhs, SocialMember rhs) {

        return collator.compare(lhs.getFullName(), rhs.getFullName());

    }
}
