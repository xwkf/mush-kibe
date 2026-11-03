package br.com.muxe.minigame.bedwars.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Payment(List<Integer> takes) {

    public Payment {
        takes = List.copyOf(takes);
    }

    public static Optional<Payment> plan(int price, List<Integer> available) {
        List<Integer> takes = new ArrayList<>(available.size());
        int left = Math.max(0, price);
        for (int amount : available) {
            int take = Math.min(Math.max(0, amount), left);
            takes.add(take);
            left -= take;
        }
        return left > 0 ? Optional.empty() : Optional.of(new Payment(takes));
    }
}
