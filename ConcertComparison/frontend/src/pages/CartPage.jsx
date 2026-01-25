import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import CartItem from '../components/cart/CartItem';
import CartSummary from '../components/cart/CartSummary';
import CartTimer from '../components/cart/CartTimer';
import { purchaseBulkTickets } from '../api/checkoutApi';

/**
 * CartPage Component
 * Shopping cart page showing all reserved seats and checkout summary
 */
const CartPage = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  const {
    cartItems,
    itemCount,
    subtotal,
    serviceFees,
    total,
    oldestItem,
    isEmpty,
    removeItem,
    clearCart,
    getAllHoldIds,
  } = useCart();

  const [isCheckingOut, setIsCheckingOut] = useState(false);
  const [checkoutError, setCheckoutError] = useState(null);

  /**
   * Handle bulk checkout
   */
  const handleCheckout = async () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/cart' } });
      return;
    }

    setIsCheckingOut(true);
    setCheckoutError(null);

    try {
      const holdIds = getAllHoldIds();
      const results = await purchaseBulkTickets(holdIds);

      // Check results
      const successCount = results.filter(r => r.success).length;
      const failCount = results.filter(r => !r.success).length;

      if (successCount > 0) {
        // Remove successfully purchased items from cart
        results.forEach(result => {
          if (result.success) {
            removeItem(result.holdId);
          }
        });

        if (failCount === 0) {
          // All purchases successful
          clearCart();
          navigate('/checkout/success', {
            state: {
              orders: results.map(r => r.data),
              totalAmount: total,
            },
          });
        } else {
          // Partial success
          setCheckoutError(
            `${successCount} Ticket(s) erfolgreich gekauft. ${failCount} fehlgeschlagen. Bitte überprüfen Sie Ihren Warenkorb.`
          );
        }
      } else {
        // All failed
        setCheckoutError(
          'Kauf fehlgeschlagen. Bitte versuchen Sie es erneut oder kontaktieren Sie den Support.'
        );
      }
    } catch (error) {
      console.error('Checkout error:', error);
      setCheckoutError(
        error.message || 'Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.'
      );
    } finally {
      setIsCheckingOut(false);
    }
  };

  /**
   * Handle item removal
   */
  const handleRemoveItem = (holdId) => {
    removeItem(holdId);
    // TODO: Optionally cancel hold on backend
  };

  return (
    <div className="min-h-screen bg-background-light dark:bg-background-dark flex flex-col">
      {/* Top Navbar */}
      <header className="sticky top-0 z-50 bg-white dark:bg-[#1a2634] border-b border-gray-200 dark:border-gray-700 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <Link to="/concerts" className="flex items-center gap-2 text-primary">
              <span className="material-symbols-outlined text-3xl">confirmation_number</span>
              <h2 className="text-slate-900 dark:text-white text-xl font-bold tracking-tight">
                TicketMaster<span className="text-primary">.Clone</span>
              </h2>
            </Link>

            {/* Right Actions */}
            <div className="flex items-center gap-6">
              <div className="flex gap-3">
                <Link to="/cart">
                  <button className="flex items-center justify-center w-10 h-10 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors relative">
                    <span className="material-symbols-outlined">shopping_cart</span>
                    {itemCount > 0 && (
                      <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-ticket-orange rounded-full border-2 border-white dark:border-[#1a2634]"></span>
                    )}
                  </button>
                </Link>
                {isAuthenticated ? (
                  <div className="relative group">
                    <button className="flex items-center justify-center w-10 h-10 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
                      <span className="material-symbols-outlined">account_circle</span>
                    </button>
                    <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-[#1a2634] rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all">
                      <div className="py-2">
                        <div className="px-4 py-2 border-b border-gray-100 dark:border-gray-700">
                          <p className="text-sm font-semibold text-slate-900 dark:text-white">
                            {user?.firstName} {user?.lastName}
                          </p>
                          <p className="text-xs text-slate-500 dark:text-slate-400">{user?.email}</p>
                        </div>
                        <button
                          onClick={() => {
                            logout();
                            navigate('/concerts');
                          }}
                          className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                        >
                          Abmelden
                        </button>
                      </div>
                    </div>
                  </div>
                ) : (
                  <Link to="/login">
                    <button className="flex items-center justify-center w-10 h-10 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors">
                      <span className="material-symbols-outlined">account_circle</span>
                    </button>
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-grow w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Page Heading & Timer */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-black tracking-tight text-slate-900 dark:text-white">
              Ihr Warenkorb
            </h1>
            <p className="text-slate-500 dark:text-slate-400 mt-1">
              Überprüfen Sie Ihre Tickets vor dem Checkout.
            </p>
          </div>

          {/* Countdown Timer */}
          {!isEmpty && oldestItem && <CartTimer expiresAt={oldestItem.expiresAt} />}
        </div>

        {/* Checkout Error */}
        {checkoutError && (
          <div className="mb-6 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4 flex items-start gap-3">
            <span className="material-symbols-outlined text-red-600 dark:text-red-400">error</span>
            <div className="flex-1">
              <h3 className="font-semibold text-red-800 dark:text-red-300">Checkout-Fehler</h3>
              <p className="text-sm text-red-700 dark:text-red-400 mt-1">{checkoutError}</p>
            </div>
            <button
              onClick={() => setCheckoutError(null)}
              className="text-red-600 dark:text-red-400 hover:text-red-800 dark:hover:text-red-300"
            >
              <span className="material-symbols-outlined">close</span>
            </button>
          </div>
        )}

        {/* Empty Cart State */}
        {isEmpty ? (
          <div className="text-center py-20">
            <span className="material-symbols-outlined text-slate-300 dark:text-slate-600 text-8xl mb-4">
              shopping_cart
            </span>
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">
              Ihr Warenkorb ist leer
            </h2>
            <p className="text-slate-500 dark:text-slate-400 mb-6">
              Fügen Sie Tickets hinzu, um mit dem Checkout fortzufahren.
            </p>
            <Link to="/concerts">
              <button className="px-6 py-3 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors font-semibold">
                Konzerte entdecken
              </button>
            </Link>
          </div>
        ) : (
          <div className="flex flex-col lg:flex-row gap-8">
            {/* Left Column: Cart Items (60%) */}
            <div className="lg:w-[60%] flex flex-col gap-6">
              {cartItems.map(item => (
                <CartItem key={item.holdId} item={item} onRemove={handleRemoveItem} />
              ))}
            </div>

            {/* Right Column: Summary (40%) */}
            <CartSummary
              subtotal={subtotal}
              serviceFees={serviceFees}
              total={total}
              itemCount={itemCount}
              onCheckout={handleCheckout}
              isLoading={isCheckingOut}
            />
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white dark:bg-[#1a2634] border-t border-gray-200 dark:border-gray-700 py-8 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            © 2024 TicketMaster Clone. Alle Rechte vorbehalten.
          </p>
          <div className="flex gap-6 text-sm text-slate-500 dark:text-slate-400">
            <Link to="/privacy" className="hover:text-primary">
              Datenschutz
            </Link>
            <Link to="/terms" className="hover:text-primary">
              AGB
            </Link>
            <button className="hover:text-primary" onClick={() => {}}>
              Hilfe
            </button>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default CartPage;
