import React from 'react';
import './MobileRoleSelector.css';

/**
 * Modal zur Auswahl der Mobile-App-Rolle
 * Wird angezeigt, wenn der Mobile-Button geklickt wird
 */
function MobileRoleSelector({ onSelectRole, onClose }) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>📱 Mobile Ansicht</h2>
          <button className="close-button" onClick={onClose}>✕</button>
        </div>
        
        <p className="modal-description">
          Wähle deine Rolle für die mobile Ansicht:
        </p>
        
        <div className="role-options">
          <button 
            className="role-option customer"
            onClick={() => onSelectRole('customer')}
          >
            <div className="role-icon">🍽️</div>
            <h3>Kunde</h3>
            <p>Essen bestellen</p>
          </button>
          
          <button 
            className="role-option staff"
            onClick={() => onSelectRole('staff')}
          >
            <div className="role-icon">👔</div>
            <h3>Mitarbeiter</h3>
            <p>QR-Code validieren</p>
          </button>
        </div>
      </div>
    </div>
  );
}

export default MobileRoleSelector;
