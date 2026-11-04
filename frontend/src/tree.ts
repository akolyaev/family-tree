/**
 * Tree rendering module for Family Tree SPA.
 * Uses Cedrine library to render the family tree as SVG.
 */

import type { PersonNode } from './api';

/**
 * Cedrine options for family tree rendering.
 */
export interface TreeOptions {
  cardWidth?: number;
  cardHeight?: number;
  xSpacing?: number;
  ySpacing?: number;
  coupleGap?: number;
  renderCard?: (person: PersonNode['person'], partner: PersonNode['partner'] | null) => HTMLElement;
  onCardClick?: (personId: number) => void;
  onCardDblClick?: (personId: number) => void;
}

/**
 * Default renderCard function for Cedrine.
 * Renders a card with photo, name, and dates.
 */
export function defaultRenderCard(
  person: PersonNode['person'],
  partner: PersonNode['partner'] | null
): HTMLElement {
  const card = document.createElement('div');
  card.className = 'ced-card';

  const photo = document.createElement('img');
  photo.className = 'ced-card-photo';
  photo.src = person.photoUrl || '';
  photo.alt = person.firstName;
  photo.onerror = () => {
    photo.style.display = 'none';
  };

  const name = document.createElement('div');
  name.className = 'ced-card-name';
  name.textContent = `${person.firstName} ${person.lastName}`;

  const dates = document.createElement('div');
  dates.className = 'ced-card-dates';
  const birthYear = person.birthDate || '—';
  const deathYear = person.deathDate || '';
  dates.textContent = deathYear ? `${birthYear} — ${deathYear}` : birthYear;

  card.appendChild(photo);
  card.appendChild(name);
  card.appendChild(dates);

  return card;
}

/**
 * Create a family tree container with Cedrine.
 * @param container - DOM element to render the tree in
 * @param options - Cedrine rendering options
 * @returns Tree instance with update/fitToView methods
 */
export function createFamilyTree(
  container: HTMLElement,
  options: TreeOptions = {}
): {
  update: (data: PersonNode) => void;
  fitToView: () => void;
  search: (query: string) => void;
  searchHighlight: (personIds: number[]) => void;
  clearHighlight: () => void;
  highlight: (personIds: number[]) => void;
} {
  const {
    cardWidth = 160,
    cardHeight = 80,
    xSpacing = 40,
    ySpacing = 120,
    coupleGap = 16,
    renderCard = defaultRenderCard,
    onCardClick,
  } = options;

  let currentData: PersonNode | null = null;
  const svgElements: SVGElement[] = [];
  const cardElements: Map<number, HTMLElement> = new Map();

  // Create SVG container
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('width', '100%');
  svg.setAttribute('height', '600');
  svg.setAttribute('viewBox', '0 0 800 600');
  svg.style.width = '100%';
  svg.style.height = '600px';
  svg.style.border = '1px solid #e0e0e0';
  svg.style.borderRadius = '8px';
  svg.style.cursor = 'grab';

  // Simple pan/zoom support
  let scale = 1;
  let translateX = 0;
  let translateY = 0;

  svg.addEventListener('wheel', (e: WheelEvent) => {
    e.preventDefault();
    const delta = e.deltaY > 0 ? 0.9 : 1.1;
    scale = Math.min(3, Math.max(0.3, scale * delta));
    svg.style.transform = `translate(${translateX}px, ${translateY}px) scale(${scale})`;
    svg.style.transformOrigin = 'center center';
  });

  // Add SVG to container
  container.appendChild(svg);

  /**
   * Render the tree from data
   */
  function renderTree(data: PersonNode): void {
    svg.innerHTML = '';
    cardElements.clear();
    svgElements.length = 0;

    const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    g.setAttribute('transform', `translate(320, 50)`);

    // Render root person
    const rootCard = renderCard(data.person, data.partner);
    rootCard.style.position = 'absolute';
    rootCard.style.width = `${cardWidth}px`;
    rootCard.style.height = `${cardHeight}px`;
    rootCard.style.cursor = 'pointer';
    rootCard.style.transition = 'box-shadow 0.2s';

    rootCard.addEventListener('click', () => {
      onCardClick?.(data.person.id);
    });

    const rootGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    rootGroup.setAttribute('transform', `translate(0, 0)`);

    // Background rect
    const bg = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
    bg.setAttribute('width', String(cardWidth));
    bg.setAttribute('height', String(cardHeight));
    bg.setAttribute('rx', '8');
    bg.setAttribute('fill', '#ffffff');
    bg.setAttribute('stroke', '#dee2e6');
    bg.setAttribute('stroke-width', '1');
    rootGroup.appendChild(bg);

    // Card content (HTML overlay)
    const overlay = document.createElement('div');
    overlay.style.position = 'absolute';
    overlay.style.top = '0';
    overlay.style.left = '0';
    overlay.style.width = `${cardWidth}px`;
    overlay.style.height = `${cardHeight}px`;
    overlay.appendChild(rootCard);
    rootGroup.appendChild(overlay as unknown as Node);

    g.appendChild(rootGroup);
    cardElements.set(data.person.id, rootCard);

    // Render partner (wife) if exists
    if (data.partner) {
      const partnerX = cardWidth + coupleGap;
      const partnerCard = renderCard(data.partner, null);
      partnerCard.style.position = 'absolute';
      partnerCard.style.width = `${cardWidth}px`;
      partnerCard.style.height = `${cardHeight}px`;
      partnerCard.style.cursor = 'pointer';

      partnerCard.addEventListener('click', () => {
        onCardClick?.(data.partner!.id);
      });

      const partnerGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      partnerGroup.setAttribute('transform', `translate(${partnerX}, 0)`);

      const pBg = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
      pBg.setAttribute('width', String(cardWidth));
      pBg.setAttribute('height', String(cardHeight));
      pBg.setAttribute('rx', '8');
      pBg.setAttribute('fill', '#ffffff');
      pBg.setAttribute('stroke', '#dee2e6');
      pBg.setAttribute('stroke-width', '1');
      partnerGroup.appendChild(pBg);

      // Connection line between spouses
      const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
      line.setAttribute('x1', String(cardWidth));
      line.setAttribute('y1', String(cardHeight / 2));
      line.setAttribute('x2', String(cardWidth + coupleGap));
      line.setAttribute('y2', String(cardHeight / 2));
      line.setAttribute('stroke', '#adb5bd');
      line.setAttribute('stroke-width', '2');
      g.appendChild(line);

      const pOverlay = document.createElement('div');
      pOverlay.style.position = 'absolute';
      pOverlay.style.top = '0';
      pOverlay.style.left = '0';
      pOverlay.style.width = `${cardWidth}px`;
      pOverlay.style.height = `${cardHeight}px`;
      pOverlay.appendChild(partnerCard);
      partnerGroup.appendChild(pOverlay as unknown as Node);

      g.appendChild(partnerGroup);
      cardElements.set(data.partner.id, partnerCard);
    }

    // Render children
    if (data.children && data.children.length > 0) {
      const childrenY = cardHeight + ySpacing;
      const totalChildrenWidth = data.children.length * cardWidth +
        (data.children.length - 1) * xSpacing;
      const startX = -totalChildrenWidth / 2;

      // Vertical line from parents
      const parentCenterX = data.partner ? cardWidth + coupleGap + cardWidth / 2 : cardWidth / 2;
      const vLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
      vLine.setAttribute('x1', String(parentCenterX));
      vLine.setAttribute('y1', String(cardHeight));
      vLine.setAttribute('x2', String(parentCenterX));
      vLine.setAttribute('y2', String(childrenY));
      vLine.setAttribute('stroke', '#adb5bd');
      vLine.setAttribute('stroke-width', '2');
      g.appendChild(vLine);

      // Horizontal line across children
      if (data.children.length > 1) {
        const hLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        hLine.setAttribute('x1', String(startX + cardWidth / 2));
        hLine.setAttribute('y1', String(childrenY));
        hLine.setAttribute('x2', String(startX + totalChildrenWidth - cardWidth / 2));
        hLine.setAttribute('y2', String(childrenY));
        hLine.setAttribute('stroke', '#adb5bd');
        hLine.setAttribute('stroke-width', '2');
        g.appendChild(hLine);
      }

      data.children.forEach((child, index) => {
        const childX = startX + index * (cardWidth + xSpacing);
        const childCard = renderCard(child, null);
        childCard.style.position = 'absolute';
        childCard.style.width = `${cardWidth}px`;
        childCard.style.height = `${cardHeight}px`;
        childCard.style.cursor = 'pointer';

        childCard.addEventListener('click', () => {
          onCardClick?.(child.id);
        });

        const childGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        childGroup.setAttribute('transform', `translate(${childX}, ${childrenY})`);

        const cBg = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
        cBg.setAttribute('width', String(cardWidth));
        cBg.setAttribute('height', String(cardHeight));
        cBg.setAttribute('rx', '8');
        cBg.setAttribute('fill', '#ffffff');
        cBg.setAttribute('stroke', '#dee2e6');
        cBg.setAttribute('stroke-width', '1');
        childGroup.appendChild(cBg);

        // Vertical connector to child
        const cLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        cLine.setAttribute('x1', String(cardWidth / 2));
        cLine.setAttribute('y1', '0');
        cLine.setAttribute('x2', String(cardWidth / 2));
        cLine.setAttribute('y2', String(childrenY - childrenY));
        cLine.setAttribute('stroke', '#adb5bd');
        cLine.setAttribute('stroke-width', '2');

        const cOverlay = document.createElement('div');
        cOverlay.style.position = 'absolute';
        cOverlay.style.top = '0';
        cOverlay.style.left = '0';
        cOverlay.style.width = `${cardWidth}px`;
        cOverlay.style.height = `${cardHeight}px`;
        cOverlay.appendChild(childCard);
        childGroup.appendChild(cOverlay as unknown as Node);

        g.appendChild(childGroup);
        cardElements.set(child.id, childCard);
      });
    }

    svg.appendChild(g);
  }

  return {
    /** Update tree with new data */
    update(data: PersonNode): void {
      currentData = data;
      renderTree(data);
      fitToView();
    },

    /** Fit tree to view container */
    fitToView(): void {
      scale = 1;
      translateX = 0;
      translateY = 0;
      svg.style.transform = 'none';
    },

    /** Search by query string */
    search(query: string): void {
      if (!query || !currentData) return;
      const q = query.toLowerCase();
      const matchedIds: number[] = [];

      if (currentData.person.firstName.toLowerCase().includes(q) ||
          currentData.person.lastName.toLowerCase().includes(q)) {
        matchedIds.push(currentData.person.id);
      }
      if (currentData.partner) {
        if (currentData.partner.firstName.toLowerCase().includes(q) ||
            currentData.partner.lastName.toLowerCase().includes(q)) {
          matchedIds.push(currentData.partner.id);
        }
      }
      currentData.children.forEach(child => {
        if (child.firstName.toLowerCase().includes(q) ||
            child.lastName.toLowerCase().includes(q)) {
          matchedIds.push(child.id);
        }
      });

      searchHighlight(matchedIds);
    },

    /** Highlight matched cards */
    searchHighlight(personIds: number[]): void {
      cardElements.forEach((card, id) => {
        if (personIds.includes(id)) {
          card.style.boxShadow = '0 0 0 3px #ffc107';
        } else {
          card.style.boxShadow = 'none';
        }
      });
    },

    /** Clear all highlights */
    clearHighlight(): void {
      cardElements.forEach(card => {
        card.style.boxShadow = 'none';
      });
    },

    /** Highlight specific person cards */
    highlight(personIds: number[]): void {
      cardElements.forEach((card, id) => {
        if (personIds.includes(id)) {
          card.style.boxShadow = '0 0 0 3px #0d6efd';
        } else {
          card.style.boxShadow = 'none';
        }
      });
    },
  };
}
