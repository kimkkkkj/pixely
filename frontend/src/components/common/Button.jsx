import { forwardRef } from 'react';
import Icon from './Icon';

/**
 * variant: primary | gradient | outline | ghost | danger | light | default
 * size: sm | md | lg
 */
const Button = forwardRef(function Button(
  { variant = 'default', size = 'md', block, icon, iconRight, loading, iconOnly, className = '', children, type = 'button', disabled, ...rest },
  ref,
) {
  const classes = [
    'px-btn',
    variant !== 'default' && `px-btn--${variant}`,
    size !== 'md' && `px-btn--${size}`,
    block && 'px-btn--block',
    iconOnly && 'px-btn--icon',
    className,
  ]
    .filter(Boolean)
    .join(' ');
  return (
    <button ref={ref} type={type} className={classes} disabled={disabled || loading} aria-busy={loading || undefined} {...rest}>
      {loading ? <span className="px-spinner" aria-hidden="true" /> : icon && <Icon name={icon} size={size === 'sm' ? 15 : 18} />}
      {children}
      {iconRight && !loading && <Icon name={iconRight} size={size === 'sm' ? 15 : 18} />}
    </button>
  );
});

export default Button;
