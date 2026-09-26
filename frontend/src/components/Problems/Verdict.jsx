function getColor(verdict) {
  if (!verdict) {
    return '#888';
  }
  const v = verdict.toLowerCase();
  if (v.includes('accept')) {
    return '#2e7d32'; // green
  }
  if (v.includes('wrong') || v.includes('error') || v.includes('runtime')) {
    return '#d32f2f'; // red
  }
  return '#ffa116'; // yellow/orange
}

function Verdict({ verdicts }) {
  return (
    <div style={{ marginTop: 16, marginBottom: 8 }}>
      {verdicts && verdicts.length > 0 ? (
        <div
          style={{
            display: 'flex',
            gap: '12px',
            flexWrap: 'wrap',
            justifyContent: 'center',
          }}
        >
          {verdicts.map((verdict, index) => {
            // Support both string and object with status
            const verdictText = verdict && typeof verdict === 'object' && verdict.status ? verdict.status : verdict;
            return (
              <div
                key={index}
                className="verdict"
                style={{
                  background: '#f5f7fa',
                  color: getColor(verdictText),
                  padding: '10px 14px',
                  borderRadius: 8,
                  fontWeight: 600,
                  fontSize: '1rem',
                  minWidth: 100,
                  textAlign: 'center',
                  boxShadow: '0 1px 4px rgba(0,0,0,0.04)',
                }}
              >
                TC {index + 1}: {verdictText}
              </div>
            );
          })}
        </div>
      ) : (
        <div
          className="verdict"
          style={{
            background: '#f5f7fa',
            color: '#888',
            padding: '10px 18px',
            borderRadius: 8,
            fontWeight: 600,
            fontSize: '1.08rem',
            minHeight: 32,
            textAlign: 'center',
            boxShadow: '0 1px 4px rgba(0,0,0,0.04)',
          }}
        >
          Verdicts will appear here.
        </div>
      )}
    </div>
  );
}

export default Verdict;
