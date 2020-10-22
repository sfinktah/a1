package controller.app;

import view.AppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClearBetButtonListener implements ActionListener {
   private final AppView appView;

   public ClearBetButtonListener(AppView appView) {
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      // Set bet value field to 0
      appView.setBetFieldValue(0);
      // Trigger a press of the Bet Set button
      ActionListener[] actionListeners = appView.getSetButton().getActionListeners();
      for (ActionListener listener : actionListeners) {
         listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;
         });
      }
   }
}
