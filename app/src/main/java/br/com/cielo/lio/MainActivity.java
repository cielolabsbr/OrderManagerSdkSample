package br.com.cielo.lio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;

import cielo.sdk.order.Credentials;
import cielo.sdk.order.Environment;
import cielo.sdk.order.Order;
import cielo.sdk.order.OrderManager;
import cielo.sdk.order.ServiceBindListener;
import cielo.sdk.order.payment.PaymentError;
import cielo.sdk.order.payment.PaymentListener;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private Button buttonTest;

    private OrderManager orderManager;
    private Order order;
    private boolean isServiceBound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViews();

        initializeOrderManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindOrderManager();
    }

    private void initializeViews() {
        setContentView(R.layout.activity_main);

        buttonTest = (Button) findViewById(R.id.button_test);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runPaymentFlow();
            }
        });
    }

    private void initializeOrderManager() {
        createOrderManager();
        bindOrderManagerToActivity();
    }

    private void createOrderManager() {
        Credentials credentials = new Credentials("<SEU_ACCESS_KEY>", "<SEU_SECRET_ACCESS_KEY>");

        Environment environment = Environment.SANDBOX;

        orderManager = new OrderManager(credentials, environment);
    }

    private void bindOrderManagerToActivity() {
        ServiceBindListener serviceBindListener = new ServiceBindListener() {
            @Override
            public void onServiceBound() {
                Log.d(LOG_TAG, "The service is bounded.");
                isServiceBound = true;
            }

            @Override
            public void onServiceUnbound() {
                Log.d(LOG_TAG, "Failed to bind to service.");
                isServiceBound = false;
            }
        };

        orderManager.bind(this, serviceBindListener);
    }

    private void runPaymentFlow() {
        //if (!isServiceBound){
        //    Log.d(LOG_TAG, "The service is not bounded to activity yet.");
        //    return;
        //}

        createOrder();

        addItemToOrder();

        updateAndPlaceOrder();

        initializeCheckout();
    }

    private void createOrder() {
        order = orderManager.createDraftOrder("<IDENTIFICADOR_DO_PEDIDO>");
    }

    private void addItemToOrder() {
        String sku = "2891820317391823"; // Identificação do produto (Stock Keeping Unit)
        String name = "Coca­cola lata";
        int unitPrice = 550; // Preço unitário em centavos
        int quantity = 3;
        String unityOfMeasure = "each"; // Unidade de medida do produto

        order.addItem(sku, name, unitPrice, quantity, unityOfMeasure);
    }

    private void updateAndPlaceOrder() {
        orderManager.updateOrder(order);
        orderManager.placeOrder(order);
    }

    private void initializeCheckout() {
        PaymentListener paymentListener = new PaymentListener() {
            @Override
            public void onStart() {
                Log.d(LOG_TAG, "O pagamento começou.");
            }

            @Override
            public void onPayment(@NotNull Order order) {
                Log.d(LOG_TAG, "Um pagamento foi realizado.");
            }

            @Override
            public void onCancel() {
                Log.d(LOG_TAG, "A operação foi cancelada.");
            }

            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "O pagamento foi realizado com sucesso.");
            }

            @Override
            public void onError(@NotNull PaymentError paymentError) {
                Log.d(LOG_TAG, "Houve um erro no pagamento.");
            }
        };

        String orderId = order.getId();
        long value = order.getPrice();

        orderManager.checkoutOrder(orderId, value, paymentListener);
    }

    private void unbindOrderManager() {
        orderManager.unbind();
    }
}
