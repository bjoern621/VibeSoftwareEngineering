import React from 'react';

const BillingForm = ({ value, onChange, disabled }) => {
  // Vereinfachtes Billing-Formular
  const handleInput = (e) => {
    onChange({ ...value, [e.target.name]: e.target.value });
  };
  return (
    <div className="mb-6">
      <h2 className="text-lg font-semibold mb-2">Billing Details</h2>
      <div className="grid grid-cols-2 gap-4">
        <input name="firstName" placeholder="First Name" className="input" value={value.firstName || ''} onChange={handleInput} disabled={disabled} />
        <input name="lastName" placeholder="Last Name" className="input" value={value.lastName || ''} onChange={handleInput} disabled={disabled} />
      </div>
      <input name="email" placeholder="Email Address" className="input mt-2" value={value.email || ''} onChange={handleInput} disabled={disabled} />
      <input name="address" placeholder="Street Address" className="input mt-2" value={value.address || ''} onChange={handleInput} disabled={disabled} />
      <div className="grid grid-cols-3 gap-2 mt-2">
        <input name="city" placeholder="City" className="input" value={value.city || ''} onChange={handleInput} disabled={disabled} />
        <input name="state" placeholder="State" className="input" value={value.state || ''} onChange={handleInput} disabled={disabled} />
        <input name="zip" placeholder="Zip" className="input" value={value.zip || ''} onChange={handleInput} disabled={disabled} />
      </div>
    </div>
  );
};

export default BillingForm;
